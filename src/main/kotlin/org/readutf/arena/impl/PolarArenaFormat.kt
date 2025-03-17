package org.readutf.arena.impl

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarReader
import net.hollowcube.polar.PolarWorld
import net.hollowcube.polar.PolarWriter
import net.hollowcube.schem.reader.SpongeSchematicReader
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import org.readutf.arena.ArenaFormat
import org.readutf.arena.ArenaTemplate
import org.readutf.arena.marker.MarkerScanner
import java.util.concurrent.CountDownLatch

object PolarArenaFormat : ArenaFormat<ArenaTemplate> {
    private const val FORMAT_ID: String = "POLAR-1"

    private val server: MinecraftServer by lazy {
        MinecraftServer.init()
    }

    override fun createArenaTemplate(
        name: String,
        spongeSchematicData: ByteArray,
    ): Result<ArenaTemplate, Throwable> =
        runCatching {
            server

            val instance = MinecraftServer.getInstanceManager().createInstanceContainer()
            val polarLoader = PolarLoader(PolarWorld())
            instance.chunkLoader = polarLoader
            instance.setChunkSupplier(::LightingChunk)

            val schematic = SpongeSchematicReader().read(spongeSchematicData)

            val markers = MarkerScanner.scanSchematic(schematic)

            val countdownLoader = CountDownLatch(0)

            val size = schematic.size()
            val chunkSizeX = (size.chunkX() shr 4) + 1
            val chunkSizeZ = (size.chunkZ() shr 4) + 1

            for (x in 0 until chunkSizeX) {
                for (y in 0 until chunkSizeZ) {
                    instance.loadChunk(chunkSizeX, chunkSizeZ)
                }
            }

            schematic.createBatch().applyUnsafe(instance, 0, 0, 0) {
                countdownLoader.countDown()
            }

            countdownLoader.await()

            val data = PolarWriter.write(polarLoader.world())
            ArenaTemplate(
                name = name,
                formatId = FORMAT_ID,
                markers = markers.associateBy { it.name },
                buildData = data,
            )
        }

    override fun loadInstance(arenaTemplate: ArenaTemplate): Result<Instance, Throwable> =
        runCatching {
            server

            val polarWorld = PolarReader.read(arenaTemplate.buildData)
            val polarLoader = PolarLoader(polarWorld)

            val instance = MinecraftServer.getInstanceManager().createInstanceContainer(polarLoader)
            instance.setChunkSupplier(::LightingChunk)
            ChunkRange.chunksInRange(0, 0, 16) { x, z -> instance.loadChunk(x, z) }
            LightingChunk.relight(instance, instance.chunks)

            return@runCatching instance
        }
}
