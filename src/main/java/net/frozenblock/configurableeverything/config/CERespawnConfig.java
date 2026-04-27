package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

public class CERespawnConfig {
    public static final BuilderCodec<CERespawnConfig> CODEC = BuilderCodec.builder(CERespawnConfig.class, CERespawnConfig::new)
        .append(new KeyedCodec<>("RadiusLimitRespawnPoint", OptionalCodec.INTEGER), (c, v) -> c.radiusLimitRespawnPoint = CEPlugin.opt(v), c -> c.radiusLimitRespawnPoint).add()
        .append(new KeyedCodec<>("MaxRespawnPointsPerPlayer", OptionalCodec.INTEGER), (c, v) -> c.maxRespawnPointsPerPlayer = CEPlugin.opt(v), c -> c.maxRespawnPointsPerPlayer).add()
        .build();

    public Optional<Integer> radiusLimitRespawnPoint = Optional.empty();
    public Optional<Integer> maxRespawnPointsPerPlayer = Optional.empty();

    public static void apply(CERespawnConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object rc = CEPlugin.getField(gc, "respawnConfig");
        if (rc == null) return;
        CEPlugin.applyIfPresent(rc, "radiusLimitRespawnPoint", cfg.radiusLimitRespawnPoint, "RespawnConfig.RadiusLimitRespawnPoint");
        CEPlugin.applyIfPresent(rc, "maxRespawnPointsPerPlayer", cfg.maxRespawnPointsPerPlayer, "RespawnConfig.MaxRespawnPointsPerPlayer");
    }
}
