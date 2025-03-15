package org.readutf.arena.requirements

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import org.readutf.arena.position.Position
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class ArenaRequirementManager {
    private val logger = LoggerFactory.getLogger(ArenaRequirementManager::class.java)
    private val positionTypes = mutableMapOf<String, List<Regex>>()

    /**
     * Converts a data class with parameters annotated [Requirement] into a list of regexes
     * that can be used to filter positions
     */
    @Throws(Exception::class)
    fun <T : BuildRequirements> generateValidators(
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

            if (classifier.isSubclassOf(BuildRequirements::class)) {
                requirements.addAll(
                    generateValidators(
                        "reserved",
                        classifier as KClass<out BuildRequirements>,
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
    fun <T : BuildRequirements> constructBuildRequirements(
        positions: Map<String, Position>,
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
                        classifier != Position::class &&
                        !classifier.isSubclassOf(BuildRequirements::class)
                )
            ) {
                return Err(Exception("Unable to construct requirement of type ${parameter.type}"))
            }

            if (classifier.isSubclassOf(BuildRequirements::class)) {
                val subClass = classifier as KClass<out BuildRequirements>
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
            val positionData = primaryConstructor.call(*parameters.toTypedArray())
            return Ok(positionData)
        } catch (e: Exception) {
            return Err(Exception("Failed to create position data: ${e.message}"))
        }
    }

    private fun getParameterForType(
        regex: Regex,
        parameter: KParameter,
        positions: Map<String, Position>,
    ): Result<Any, Throwable> {
        val isList = parameter.type.classifier == List::class

        if (isList) {
            val values = positions.filter { it.key.matches(regex) }.values

            if (values.isEmpty()) {
                return Err(Exception("No positions found for ${parameter.name} matching $regex"))
            }
            return Ok(values.toList())
        } else {
            val multipleOptions = positions.filter { it.key.matches(regex) }.values

            if (multipleOptions.isEmpty()) {
                return Err(Exception("No positions found for ${parameter.name} matching $regex"))
            }
            if (multipleOptions.size > 1) {
                logger.warn("Multiple positions found for ${parameter.name} matching $regex")
            }

            val value = multipleOptions.first()

            return Ok(value)
        }
    }
}
