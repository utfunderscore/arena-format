package org.readutf.arena

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import org.readutf.arena.requirements.ArenaPositions
import org.readutf.arena.requirements.ArenaRequirementManager
import kotlin.reflect.KClass

class ArenaManager<T : ArenaTemplate>(
    private val arenaFormat: ArenaFormat<T>,
) {
    fun createTemplate(
        name: String,
        spongeSchematicData: ByteArray,
    ) = arenaFormat.createArenaTemplate(name, spongeSchematicData)

    inline fun <reified POSITIONS : ArenaPositions> loadArena(template: T): Result<Arena<POSITIONS>, Throwable> =
        loadArena(template, POSITIONS::class)

    fun <REQUIREMENTS : ArenaPositions> loadArena(
        template: T,
        type: KClass<REQUIREMENTS>,
    ): Result<Arena<REQUIREMENTS>, Throwable> {
        val instance = arenaFormat.loadInstance(template).getOrElse { return Err(it) }

        val arenaPositions =
            ArenaRequirementManager.constructBuildRequirements(template.markers, type).getOrElse {
                return Err(it)
            }

        return Ok(
            Arena(
                world = instance,
                arenaRequirements = arenaPositions,
            ),
        )
    }
}
