package net.frozenblock.configurableeverything;

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.util.Config;
import org.jspecify.annotations.NonNull;

public class CEPlugin extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Config<CEConfig> config = this.withConfig(CEConfig.CODEC);

    public CEPlugin(@NonNull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new PoopCommand());
    }

    @Override
    protected void start() {
        if (this.config.load().join().worldGenV2) {
            worldGenV2();
        } else {
            LOGGER.atInfo().log("Sticking to World Gen V1");
        }

        this.config.save().join();
    }

    private void worldGenV2() {
        LOGGER.atInfo().log("Enabling World Gen V2");

        var V2 = IWorldGenProvider.CODEC.getCodecFor("HytaleGenerator");

        IWorldGenProvider.CODEC.remove(HandleProvider.class);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(2), "HytaleGenerator", HandleProvider.class, V2);
    }

    public static class CEConfig {

        public static final BuilderCodec<CEConfig> CODEC = BuilderCodec.builder(
            CEConfig.class, CEConfig::new
        )
        .append(
            new KeyedCodec<>("WorldGenV2", Codec.BOOLEAN),
            (config, value) -> config.worldGenV2 = value,
            config -> config.worldGenV2
        )
        .add()
        .build();

        public boolean worldGenV2 = false;
    }
}
