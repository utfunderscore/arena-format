package org.readutf.arena

import com.github.michaelbull.result.Result
import org.readutf.arena.world.PlatformWorld

interface ArenaFormat<TEMPLATE : ArenaTemplate> {
    fun createArenaTemplate(
        name: String,
        spongeSchematicData: ByteArray,
    ): Result<TEMPLATE, Throwable>

    fun loadInstance(arenaTemplate: TEMPLATE): Result<PlatformWorld, Throwable>
}
