package org.readutf.arena.marker

import net.hollowcube.schem.Schematic
import net.minestom.server.coordinate.Point

object MarkerScanner {
    /**
     * Takes an input schematic, scans across all tile entities and returns any valid markers
     */
    fun scanSchematic(schematic: Schematic): List<Marker> {
        val blockEntities = schematic.blockEntities().associateBy { it -> it.position }
        val blocks = mutableMapOf<Point, MarkerRotation>()
        schematic.forEachBlock { point, block ->
            if (blockEntities.containsKey(point)) {
                blocks[point] = MarkerRotation.entries.getOrNull(block.getProperty("rotation").toIntOrNull() ?: 0) ?: MarkerRotation.NORTH
            }
        }
        return blockEntities.mapNotNull { (point, entity) ->
            val rotation = blocks[point] ?: MarkerRotation.NORTH
            Marker.fromSign(entity, rotation)
        }
    }
}
