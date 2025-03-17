package org.readutf.arena.utils

import com.github.michaelbull.result.onSuccess
import org.readutf.arena.requirements.ArenaPositions
import org.readutf.arena.requirements.ArenaRequirementManager
import java.io.File
import kotlin.reflect.KClass

object RequirementsUtils {
    fun exportToFile(
        name: String,
        arenaPositions: KClass<out ArenaPositions>,
    ) {
        ArenaRequirementManager.generateRequirements(name, arenaPositions).onSuccess {
            File("$name-requirements.txt").writeText(it.joinToString("\n"))
        }
    }

    fun generateChecksum(regexs: List<Regex>): Int = regexs.joinToString().hashCode()
}
