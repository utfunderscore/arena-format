package org.readutf.arena.utils

import com.github.michaelbull.result.onSuccess
import org.readutf.arena.requirements.ArenaPositions
import org.readutf.arena.requirements.ArenaRequirementManager
import java.io.File
import kotlin.reflect.KClass

object ArenaRequirementsExporter {
    fun export(
        name: String,
        arenaPositions: KClass<ArenaPositions>,
    ) {
        ArenaRequirementManager.generateRequirements(name, arenaPositions).onSuccess {
            File("$name-requirements.txt").writeText(it.joinToString("\n"))
        }
    }
}
