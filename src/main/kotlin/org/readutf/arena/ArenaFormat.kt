package org.readutf.arena

import com.github.michaelbull.result.Result
import net.minestom.server.instance.Instance

interface ArenaFormat<TEMPLATE : ArenaTemplate> {
    fun createArenaTemplate(
        name: String,
        spongeSchematicData: ByteArray,
    ): Result<TEMPLATE, Throwable>

    fun loadInstance(arenaTemplate: TEMPLATE): Result<Instance, Throwable>
}
