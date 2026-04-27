package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

public class CEWorldConfig {
    public static final BuilderCodec<CEWorldConfig> CODEC = BuilderCodec.builder(CEWorldConfig.class, CEWorldConfig::new)
        .append(new KeyedCodec<>("DaytimeDurationSeconds", OptionalCodec.INTEGER), (c, v) -> c.daytimeDurationSeconds = CEPlugin.opt(v), c -> c.daytimeDurationSeconds).add()
        .append(new KeyedCodec<>("NighttimeDurationSeconds", OptionalCodec.INTEGER), (c, v) -> c.nighttimeDurationSeconds = CEPlugin.opt(v), c -> c.nighttimeDurationSeconds).add()
        .append(new KeyedCodec<>("AllowBlockBreaking", OptionalCodec.BOOLEAN), (c, v) -> c.allowBlockBreaking = CEPlugin.opt(v), c -> c.allowBlockBreaking).add()
        .append(new KeyedCodec<>("AllowBlockGathering", OptionalCodec.BOOLEAN), (c, v) -> c.allowBlockGathering = CEPlugin.opt(v), c -> c.allowBlockGathering).add()
        .append(new KeyedCodec<>("AllowBlockPlacement", OptionalCodec.BOOLEAN), (c, v) -> c.allowBlockPlacement = CEPlugin.opt(v), c -> c.allowBlockPlacement).add()
        .build();

    public Optional<Integer> daytimeDurationSeconds = Optional.empty();
    public Optional<Integer> nighttimeDurationSeconds = Optional.empty();
    public Optional<Boolean> allowBlockBreaking = Optional.empty();
    public Optional<Boolean> allowBlockGathering = Optional.empty();
    public Optional<Boolean> allowBlockPlacement = Optional.empty();

    public static void apply(CEWorldConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object wc = CEPlugin.getField(gc, "worldConfig");
        if (wc == null) return;
        CEPlugin.applyIfPresent(wc, "daytimeDurationSeconds", cfg.daytimeDurationSeconds, "WorldConfig.DaytimeDurationSeconds");
        CEPlugin.applyIfPresent(wc, "nighttimeDurationSeconds", cfg.nighttimeDurationSeconds, "WorldConfig.NighttimeDurationSeconds");
        CEPlugin.applyIfPresent(wc, "allowBlockBreaking", cfg.allowBlockBreaking, "WorldConfig.AllowBlockBreaking");
        CEPlugin.applyIfPresent(wc, "allowBlockGathering", cfg.allowBlockGathering, "WorldConfig.AllowBlockGathering");
        CEPlugin.applyIfPresent(wc, "allowBlockPlacement", cfg.allowBlockPlacement, "WorldConfig.AllowBlockPlacement");
    }
}
