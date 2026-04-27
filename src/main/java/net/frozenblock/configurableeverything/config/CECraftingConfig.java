package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

public class CECraftingConfig {
    public static final BuilderCodec<CECraftingConfig> CODEC = BuilderCodec.builder(CECraftingConfig.class, CECraftingConfig::new)
        .append(new KeyedCodec<>("ChestHorizontalSearchRadius", OptionalCodec.INTEGER), (c, v) -> c.chestHorizontalSearchRadius = CEPlugin.opt(v), c -> c.chestHorizontalSearchRadius).add()
        .append(new KeyedCodec<>("ChestVerticalSearchRadius", OptionalCodec.INTEGER), (c, v) -> c.chestVerticalSearchRadius = CEPlugin.opt(v), c -> c.chestVerticalSearchRadius).add()
        .append(new KeyedCodec<>("ChestLimit", OptionalCodec.INTEGER), (c, v) -> c.chestLimit = CEPlugin.opt(v), c -> c.chestLimit).add()
        .build();

    public Optional<Integer> chestHorizontalSearchRadius = Optional.empty();
    public Optional<Integer> chestVerticalSearchRadius  = Optional.empty();
    public Optional<Integer> chestLimit = Optional.empty();

    public static void apply(CECraftingConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object cc = CEPlugin.getField(gc, "craftingConfig");
        if (cc == null) return;
        CEPlugin.applyIfPresent(cc, "benchMaterialHorizontalChestSearchRadius", cfg.chestHorizontalSearchRadius, "CraftingConfig.ChestHorizontalSearchRadius");
        CEPlugin.applyIfPresent(cc, "benchMaterialVerticalChestSearchRadius", cfg.chestVerticalSearchRadius, "CraftingConfig.ChestVerticalSearchRadius");
        CEPlugin.applyIfPresent(cc, "benchMaterialChestLimit", cfg.chestLimit, "CraftingConfig.ChestLimit");
    }
}
