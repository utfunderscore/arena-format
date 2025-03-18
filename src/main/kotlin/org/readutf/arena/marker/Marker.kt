package org.readutf.arena.marker

import net.hollowcube.schem.BlockEntityData
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import org.slf4j.LoggerFactory

/**
 * #marker - marker tag
 * test - marker name
 * 0 5 0 - offset
 */
data class Marker(
    val name: String,
    val targetPosition: Point,
    val originalPosition: Point,
    val signLines: List<String>,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(Marker::class.java)

        fun fromSign(blockEntity: BlockEntityData): Marker? {
            val signLines = extractMarkerLines(blockEntity.data)

            if (signLines.size != 4 || !signLines[0].equals("#marker", true)) {
                logger.info("Invalid sign for marker at ${blockEntity.position}")
                return null
            }

            val position = blockEntity.position
            val name: String = signLines[1]

            val offsetParts = signLines[2].split(" ", limit = 3).map { it.toDoubleOrNull() ?: 0.0 }

            val offset =
                if (offsetParts.size == 3) {
                    Pos(offsetParts[0], offsetParts[1], offsetParts[2])
                } else {
                    logger.warn("Invalid offset for marker $name")
                    Pos(0.0, 0.0, 0.0)
                }

            return Marker(name, position, position.add(offset), signLines)
        }

        private fun extractMarkerLines(compoundBinaryTag: CompoundBinaryTag): List<String> =
            compoundBinaryTag
                .getCompound("front_text")
                .getList("messages")
                .map {
                    (it as StringBinaryTag).value()
                }.map {
                    it.substring(1, it.length - 1)
                }
    }
}
