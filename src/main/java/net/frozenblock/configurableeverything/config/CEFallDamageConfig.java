package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

public class CEFallDamageConfig {
    public static final BuilderCodec<CEFallDamageConfig> CODEC = BuilderCodec.builder(CEFallDamageConfig.class, CEFallDamageConfig::new)
        .append(new KeyedCodec<>("MinFallSpeedToEngageRoll", OptionalCodec.FLOAT), (c, v) -> c.minFallSpeedToEngageRoll = CEPlugin.opt(v), c -> c.minFallSpeedToEngageRoll).add()
        .append(new KeyedCodec<>("MaxFallSpeedToEngageRoll", OptionalCodec.FLOAT), (c, v) -> c.maxFallSpeedToEngageRoll = CEPlugin.opt(v), c -> c.maxFallSpeedToEngageRoll).add()
        .append(new KeyedCodec<>("FallDamagePartialMitigationPercent", OptionalCodec.FLOAT), (c, v) -> c.fallDamagePartialMitigationPercent = CEPlugin.opt(v), c -> c.fallDamagePartialMitigationPercent).add()
        .append(new KeyedCodec<>("MaxFallSpeedRollFullMitigation", OptionalCodec.FLOAT), (c, v) -> c.maxFallSpeedRollFullMitigation = CEPlugin.opt(v), c -> c.maxFallSpeedRollFullMitigation).add()
        .append(new KeyedCodec<>("RollStartSpeedModifier", OptionalCodec.FLOAT), (c, v) -> c.rollStartSpeedModifier = CEPlugin.opt(v), c -> c.rollStartSpeedModifier).add()
        .append(new KeyedCodec<>("RollExitSpeedModifier", OptionalCodec.FLOAT), (c, v) -> c.rollExitSpeedModifier = CEPlugin.opt(v), c -> c.rollExitSpeedModifier).add()
        .append(new KeyedCodec<>("RollTimeToComplete", OptionalCodec.FLOAT), (c, v) -> c.rollTimeToComplete = CEPlugin.opt(v), c -> c.rollTimeToComplete).add()
        .build();

    public Optional<Float> minFallSpeedToEngageRoll = Optional.empty();
    public Optional<Float> maxFallSpeedToEngageRoll = Optional.empty();
    public Optional<Float> fallDamagePartialMitigationPercent = Optional.empty();
    public Optional<Float> maxFallSpeedRollFullMitigation = Optional.empty();
    public Optional<Float> rollStartSpeedModifier = Optional.empty();
    public Optional<Float> rollExitSpeedModifier = Optional.empty();
    public Optional<Float> rollTimeToComplete = Optional.empty();

    public static void apply(CEFallDamageConfig cfg) {
        if (cfg == null) return;
        MovementConfig mc = MovementConfig.getAssetMap().getAsset("Default");
        if (mc == null) {
            CEPlugin.LOGGER.atWarning().log("Could not find 'Default' MovementConfig");
            return;
        }
        CEPlugin.applyIfPresent(mc, "minFallSpeedToEngageRoll", cfg.minFallSpeedToEngageRoll, "MinFallSpeedToEngageRoll");
        CEPlugin.applyIfPresent(mc, "maxFallSpeedToEngageRoll", cfg.maxFallSpeedToEngageRoll, "MaxFallSpeedToEngageRoll");
        CEPlugin.applyIfPresent(mc, "fallDamagePartialMitigationPercent", cfg.fallDamagePartialMitigationPercent, "FallDamagePartialMitigationPercent");
        CEPlugin.applyIfPresent(mc, "maxFallSpeedRollFullMitigation", cfg.maxFallSpeedRollFullMitigation, "MaxFallSpeedRollFullMitigation");
        CEPlugin.applyIfPresent(mc, "rollStartSpeedModifier", cfg.rollStartSpeedModifier, "RollStartSpeedModifier");
        CEPlugin.applyIfPresent(mc, "rollExitSpeedModifier", cfg.rollExitSpeedModifier, "RollExitSpeedModifier");
        CEPlugin.applyIfPresent(mc, "rollTimeToComplete", cfg.rollTimeToComplete, "RollTimeToComplete");
    }
}
