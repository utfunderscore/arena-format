package org.readutf.arena.marker

import net.hollowcube.schem.Schematic

object MarkerScanner {
    /**
     * Takes an input schematic, scans across all tile entities and returns any valid markers
     */
    fun scanSchematic(schematic: Schematic) = schematic.blockEntities().mapNotNull(Marker.Companion::fromSign)
}
