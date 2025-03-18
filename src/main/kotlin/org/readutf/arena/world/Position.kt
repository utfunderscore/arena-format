package org.readutf.arena.world

import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos

data class Position(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    fun add(other: Position): Position = Position(x + other.x, y + other.y, z + other.z)

    companion object {
        fun fromSchematicPosition(schematicBlockPos: SchematicBlockPos): Position =
            Position(schematicBlockPos.x.toDouble(), schematicBlockPos.y.toDouble(), schematicBlockPos.z.toDouble())
    }
}
