package net.frozenblock.configurableeverything

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider
import com.hypixel.hytale.codec.lookup.Priority
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider


class CEPlugin(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    init {
        LOGGER.atInfo().log("Hello from %s version %s", this.name, this.manifest.version.toString())
    }

    override fun setup() {
        this.commandRegistry.registerCommand(PoopCommand())

        // TODO: Add config
        worldGenV2()
    }

    private fun worldGenV2() {
        val V1 = IWorldGenProvider.CODEC.getCodecFor("Hytale")
        val V2 = IWorldGenProvider.CODEC.getCodecFor("HytaleGenerator")

        IWorldGenProvider.CODEC.remove(HytaleWorldGenProvider::class.java)
        IWorldGenProvider.CODEC.remove(HandleProvider::class.java)

        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "HytaleGenerator", HandleProvider::class.java, V2)
        IWorldGenProvider.CODEC.register("Hytale", HytaleWorldGenProvider::class.java, V1)
    }
}
