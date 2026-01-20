package net.frozenblock.configurableeverything

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec
import com.hypixel.hytale.codec.lookup.Priority
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider
import com.hypixel.hytale.server.core.util.Config
import com.hypixel.hytale.server.npc.NPCPlugin
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider
import kotlin.properties.Delegates


class CEPlugin(init: JavaPluginInit) : JavaPlugin(init) {

    private val config: Config<CEConfig> = this.withConfig(CEConfig.CODEC)

    companion object {
        val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    init {
        LOGGER.atInfo().log("Hello from %s version %s", this.name, this.manifest.version.toString())
    }

    override fun setup() {
        this.commandRegistry.registerCommand(PoopCommand())

        // TODO: Add config
        if (config.load().join().worldGenV2)
            worldGenV2()
        else
            LOGGER.atInfo().log("Sticking to World Gen V1")

        config.save().join()
    }

    private fun worldGenV2() {
        LOGGER.atInfo().log("Enabling World Gen V2")

        val V2 = IWorldGenProvider.CODEC.getCodecFor("HytaleGenerator")

        IWorldGenProvider.CODEC.remove(HandleProvider::class.java)

        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(2), "HytaleGenerator", HandleProvider::class.java, V2)
    }
}

class CEConfig {
    companion object {
        val CODEC: BuilderCodec<CEConfig> = BuilderCodec.builder(
            CEConfig::class.java, ::CEConfig
        )
        .append(
            KeyedCodec("WorldGenV2", Codec.BOOLEAN),
            CEConfig::worldGenV2::set,
            CEConfig::worldGenV2
        )
        .add()
        .build()
    }

    var worldGenV2 = false
}