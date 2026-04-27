package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

import static net.frozenblock.configurableeverything.CEPlugin.applyIfPresent;
import static net.frozenblock.configurableeverything.CEPlugin.getField;

public class CECombatConfig {
    public static final BuilderCodec<CECombatConfig> CODEC = BuilderCodec.builder(CECombatConfig.class, CECombatConfig::new)
        .append(new KeyedCodec<>("DisplayHealthBars", OptionalCodec.BOOLEAN), (c, v) -> c.displayHealthBars = CEPlugin.opt(v), c -> c.displayHealthBars).add()
        .append(new KeyedCodec<>("DisplayCombatText", OptionalCodec.BOOLEAN), (c, v) -> c.displayCombatText = CEPlugin.opt(v), c -> c.displayCombatText).add()
        .append(new KeyedCodec<>("DisableNPCDamage", OptionalCodec.BOOLEAN), (c, v) -> c.disableNpcDamage = CEPlugin.opt(v), c -> c.disableNpcDamage).add()
        .append(new KeyedCodec<>("DisablePlayerDamage", OptionalCodec.BOOLEAN), (c, v) -> c.disablePlayerDamage = CEPlugin.opt(v), c -> c.disablePlayerDamage).add()
        .build();

    public Optional<Boolean> displayHealthBars = Optional.empty();
    public Optional<Boolean> displayCombatText = Optional.empty();
    public Optional<Boolean> disableNpcDamage = Optional.empty();
    public Optional<Boolean> disablePlayerDamage = Optional.empty();

    public static void apply(CECombatConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object cc = getField(gc, "combatConfig");
        if (cc == null) return;
        applyIfPresent(cc, "displayHealthBars", cfg.displayHealthBars, "CombatConfig.DisplayHealthBars");
        applyIfPresent(cc, "displayCombatText", cfg.displayCombatText, "CombatConfig.DisplayCombatText");
        applyIfPresent(cc, "disableNpcIncomingDamage", cfg.disableNpcDamage, "CombatConfig.DisableNPCDamage");
        applyIfPresent(cc, "disablePlayerIncomingDamage", cfg.disablePlayerDamage, "CombatConfig.DisablePlayerDamage");
    }
}
