package org.readutf.arena.requirements

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import net.minestom.server.coordinate.Pos
import org.readutf.arena.marker.Marker
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

object ArenaRequirementManager {
    private val logger = LoggerFactory.getLogger(ArenaRequirementManager::class.java)
    private val positionTypes = mutableMapOf<String, List<Regex>>()

    /**
     * Converts a data class with parameters annotated [Requirement] into a list of regexes
     * that can be used to filter positions
     */
    fun <T : ArenaPositions> generateRequirements(
        gameType: String,
        positionRequirements: KClass<T>,
    ): Result<List<Regex>, Throwable> {
        logger.info("Registering requirements for ${positionRequirements.simpleName}")

        val primary =
            positionRequirements.primaryConstructor ?: let {
                return Err(Exception("Primary constructor not found"))
            }

        val requirements = mutableListOf<Regex>()

        for (parameter in primary.parameters) {
            val classifier = parameter.type.classifier
            if (classifier !is KClass<*>) {
                return Err(Exception("${parameter.name} is not a valid type"))
            }

            if (classifier.isSubclassOf(ArenaPositions::class)) {
                requirements.addAll(
                    generateRequirements(
                        "reserved",
                        classifier as KClass<out ArenaPositions>,
                    ).getOrElse { return Err(it) },
                )
                continue
            }

            val annotation =
                parameter.findAnnotation<Requirement>()
                    ?: return Err(
                        Exception("${parameter.name} in ${positionRequirements::class.simpleName} is missing the @Position annotation"),
                    )

            when {
                annotation.name != "" -> requirements.add(Regex("^${annotation.name}$"))
                annotation.startsWith != "" -> requirements.add(Regex("${annotation.startsWith}.*"))
                annotation.endsWith != "" -> requirements.add(Regex(".*${annotation.endsWith}"))
                else -> {
                    return Err(Exception("Invalid position annotation, a filter must be set"))
                }
            }
        }

        positionTypes[gameType] = requirements
        positionTypes[positionRequirements.qualifiedName!!] = requirements

        return Ok(requirements)
    }

    /**
     * Converts a map of positions into the build requirements class
     */
    fun <T : ArenaPositions> constructBuildRequirements(
        positions: Map<String, Marker>,
        positionSettingsType: KClass<out T>,
    ): Result<T, Throwable> {
        if (positionSettingsType.qualifiedName == null) {
            return Err(Exception("Invalid position settings type"))
        }

        val primaryConstructor =
            positionSettingsType.primaryConstructor ?: let {
                return Err(Exception("Primary constructor not found"))
            }

        val parameters = mutableListOf<Any>()

        for (parameter in primaryConstructor.parameters) {
            val classifier = parameter.type.classifier
            if (classifier !is KClass<*> ||
                (
                    classifier != List::class &&
                        classifier != Marker::class &&
                        classifier != Pos::class &&
                        !classifier.isSubclassOf(ArenaPositions::class)
                )
            ) {
                return Err(Exception("Unable to construct requirement of type ${parameter.type}"))
            }

            if (classifier.isSubclassOf(ArenaPositions::class)) {
                val subClass = classifier as KClass<out ArenaPositions>
                parameters.add(constructBuildRequirements(positions, subClass).getOrElse { return Err(it) })
            } else {
                val annotation =
                    parameter.findAnnotation<Requirement>() ?: let {
                        return Err(Exception("Missing @Position annotation"))
                    }
                val regex =
                    when {
                        annotation.name != "" -> Regex("^${annotation.name}$")
                        annotation.startsWith != "" -> Regex("${annotation.startsWith}.*")
                        annotation.endsWith != "" -> Regex(".*${annotation.endsWith}")
                        else -> {
                            return Err(Exception("Invalid position annotation, a filter must be set"))
                        }
                    }
                parameters.add(getParameterForType(regex, parameter, positions).getOrElse { return Err(it) })
            }
        }

        try {
            logger.debug("Constructing with parameters: {}", parameters.map { it.javaClass.simpleName })
            val positionData = primaryConstructor.call(*parameters.toTypedArray())
            return Ok(positionData)
        } catch (e: Exception) {
            return Err(Exception("Failed to create position data: ${e.message}"))
        }
    }

    private fun getParameterForType(
        regex: Regex,
        parameter: KParameter,
        positions: Map<String, Marker>,
    ): Result<Any, Throwable> {
        val isList = parameter.type.classifier == List::class
        val isPos = parameter.type.classifier == Pos::class

        if (isList) {
            val values = positions.filter { it.key.matches(regex) }.values

            if (values.isEmpty()) {
                return Err(Exception("No positions found for ${parameter.name} matching $regex"))
            }

            return if (isPos) {
                Ok(values.toList().map { Pos.fromPoint(it.targetPosition) })
            } else {
                Ok(values.toList())
            }
        } else {
            val multipleOptions = positions.filter { it.key.matches(regex) }.values

            if (multipleOptions.isEmpty()) {
                return Err(Exception("No positions found for ${parameter.name} matching $regex"))
            }
            if (multipleOptions.size > 1) {
                logger.warn("Multiple positions found for ${parameter.name} matching $regex")
            }

            return if (isPos) {
                Ok(Pos.fromPoint(multipleOptions.first().targetPosition))
            } else {
                Ok(multipleOptions.first())
            }
        }
    }
}
