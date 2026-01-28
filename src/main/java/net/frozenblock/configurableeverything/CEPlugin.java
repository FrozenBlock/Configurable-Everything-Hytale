package net.frozenblock.configurableeverything;

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.util.Config;
import net.frozenblock.configurableeverything.scripting.util.ScriptingUtil;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
public class CEPlugin extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final File HYTALE_JAR;
    static {
        try {
            HYTALE_JAR = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static CEPlugin INSTANCE = null;

    private final Config<CEConfig> config = this.withConfig(CEConfig.CODEC);

    public CEPlugin(@NonNull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        ScriptingUtil.INSTANCE.runScripts();
        this.getCommandRegistry().registerCommand(new PoopCommand());
    }

    @Override
    protected void start() {
        worldGenV2();

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
