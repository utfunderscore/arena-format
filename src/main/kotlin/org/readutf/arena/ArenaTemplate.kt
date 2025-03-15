package org.readutf.arena

import org.readutf.arena.position.Position

data class ArenaTemplate(
    val name: String,
    val formatId: Int,
    val positions: Map<String, Position>,
)
