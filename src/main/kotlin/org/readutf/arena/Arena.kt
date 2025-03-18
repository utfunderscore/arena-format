package org.readutf.arena

import org.readutf.arena.requirements.ArenaPositions
import org.readutf.arena.world.PlatformWorld

class Arena<T : ArenaPositions>(
    val world: PlatformWorld,
    val arenaRequirements: T,
)
