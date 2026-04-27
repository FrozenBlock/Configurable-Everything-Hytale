package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

public class CEDeathConfig {
    public static final BuilderCodec<CEDeathConfig> CODEC = BuilderCodec.builder(CEDeathConfig.class, CEDeathConfig::new)
        .append(new KeyedCodec<>("ItemsLossMode", OptionalCodec.STRING), (c, v) -> c.itemsLossMode = CEPlugin.opt(v), c -> c.itemsLossMode).add()
        .append(new KeyedCodec<>("ItemsAmountLossPercentage", OptionalCodec.DOUBLE), (c, v) -> c.itemsAmountLossPercentage = CEPlugin.opt(v), c -> c.itemsAmountLossPercentage).add()
        .append(new KeyedCodec<>("ItemsDurabilityLossPercentage", OptionalCodec.DOUBLE), (c, v) -> c.itemsDurabilityLossPercentage = CEPlugin.opt(v), c -> c.itemsDurabilityLossPercentage).add()
        .build();

    /** "NONE" | "ALL" | "CONFIGURED" */
    public Optional<String> itemsLossMode = Optional.empty();
    public Optional<Double> itemsAmountLossPercentage = Optional.empty();
    public Optional<Double> itemsDurabilityLossPercentage = Optional.empty();

    public static void apply(CEDeathConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object dc = CEPlugin.getField(gc, "deathConfig");
        if (dc == null) return;
        CEPlugin.applyIfPresent(dc, "itemsAmountLossPercentage", cfg.itemsAmountLossPercentage, "DeathConfig.ItemsAmountLossPercentage");
        CEPlugin.applyIfPresent(dc, "itemsDurabilityLossPercentage", cfg.itemsDurabilityLossPercentage, "DeathConfig.ItemsDurabilityLossPercentage");
        cfg.itemsLossMode.ifPresent(mode -> {
            try {
                CEPlugin.setField(dc, "itemsLossMode", DeathConfig.ItemsLossMode.valueOf(mode.toUpperCase()));
                CEPlugin.LOGGER.atInfo().log("Set DeathConfig.ItemsLossMode to %s", mode);
            } catch (IllegalArgumentException e) {
                CEPlugin.LOGGER.atWarning().log("Unknown ItemsLossMode '%s' — valid: NONE, ALL, CONFIGURED", mode);
            }
        });
    }
}
