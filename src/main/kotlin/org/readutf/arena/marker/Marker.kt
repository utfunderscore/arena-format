package org.readutf.arena.marker

import net.sandrohc.schematic4j.schematic.types.SchematicBlockEntity
import org.readutf.arena.world.Position
import org.slf4j.LoggerFactory

/**
 * #marker - marker tag
 * test - marker name
 * 0 5 0 - offset
 */
data class Marker(
    val name: String,
    val targetPosition: Position,
    val originalPosition: Position,
    val signLines: List<String>,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(Marker::class.java)

        fun fromSign(blockEntity: SchematicBlockEntity): Marker? {
            val signLines = extractMarkerLines(blockEntity.data)
            val position = Position.fromSchematicPosition(blockEntity.pos())

            println(signLines)
            if (signLines.size != 4 || !signLines[0].equals("#marker", true)) {
                logger.info("Invalid sign for marker at $position")
                return null
            }

            val name: String = signLines[1]

            val offsetParts = signLines[2].split(" ", limit = 3).map { it.toDoubleOrNull() ?: 0.0 }

            val offset =
                if (offsetParts.size == 3) {
                    Position(offsetParts[0], offsetParts[1], offsetParts[2])
                } else {
                    logger.warn("Invalid offset for marker $name")
                    Position(0.0, 0.0, 0.0)
                }

            return Marker(name, position, position.add(offset), signLines)
        }

        private fun extractMarkerLines(compoundBinaryTag: Map<String, Any>): List<String> {
            val frontText = compoundBinaryTag["front_text"] as Map<*, *>? ?: return emptyList()
            val rawMessages = frontText["messages"] as List<*>? ?: return emptyList()
            val lines = rawMessages.map { line -> line.toString() }
            return lines
        }
    }
}
