package org.readutf.arena

import net.minestom.server.instance.Instance
import org.readutf.arena.requirements.ArenaPositions

class Arena<T : ArenaPositions>(
    val instance: Instance,
    val arenaRequirements: T,
)
