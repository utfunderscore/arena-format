package org.readutf.arena

import org.readutf.arena.marker.Marker

open class ArenaTemplate(
    val name: String,
    val formatId: String,
    val markers: Map<String, Marker>,
    val buildData: ByteArray,
)
