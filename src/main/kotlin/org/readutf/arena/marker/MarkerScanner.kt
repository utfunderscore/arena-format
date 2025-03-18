package org.readutf.arena.marker

import net.sandrohc.schematic4j.schematic.Schematic

object MarkerScanner {
    /**
     * Takes an input schematic, scans across all tile entities and returns any valid markers
     */
    fun scanSchematic(schematic: Schematic) = schematic.blockEntities().toList().mapNotNull(Marker.Companion::fromSign)
}
