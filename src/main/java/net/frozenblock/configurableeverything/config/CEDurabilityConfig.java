package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

public class CEDurabilityConfig {
    public static final BuilderCodec<CEDurabilityConfig> CODEC = BuilderCodec.builder(CEDurabilityConfig.class, CEDurabilityConfig::new)
        .append(new KeyedCodec<>("WeaponPenalty", OptionalCodec.DOUBLE), (c, v) -> c.weaponPenalty = CEPlugin.opt(v), c -> c.weaponPenalty).add()
        .append(new KeyedCodec<>("ArmorPenalty", OptionalCodec.DOUBLE), (c, v) -> c.armorPenalty = CEPlugin.opt(v), c -> c.armorPenalty).add()
        .append(new KeyedCodec<>("ToolPenalty", OptionalCodec.DOUBLE), (c, v) -> c.toolPenalty = CEPlugin.opt(v), c -> c.toolPenalty).add()
        .build();

    public Optional<Double> weaponPenalty = Optional.empty();
    public Optional<Double> armorPenalty = Optional.empty();
    public Optional<Double> toolPenalty = Optional.empty();

    public static void apply(CEDurabilityConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object idc = CEPlugin.getField(gc, "itemDurabilityConfig");
        if (idc == null) return;
        Object bp = CEPlugin.getField(idc, "brokenPenalties");
        if (bp == null) return;
        CEPlugin.applyIfPresent(bp, "weapon", cfg.weaponPenalty, "BrokenPenalties.weapon");
        CEPlugin.applyIfPresent(bp, "armor", cfg.armorPenalty, "BrokenPenalties.armor");
        CEPlugin.applyIfPresent(bp, "tool", cfg.toolPenalty, "BrokenPenalties.tool");
    }
}
