package net.frozenblock.configurableeverything;

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.suppliers.ItemRepairPageSupplier;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.util.Config;
import net.frozenblock.configurableeverything.codec.OptionalCodec;
import net.frozenblock.configurableeverything.codec.NullableCodec;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        this.getCommandRegistry().registerCommand(new PoopCommand());
    }

    @Override
    protected void start() {
        CEConfig cfg = this.config.get();
        if (cfg.worldGenV2) {
            worldGenV2();
        }
        writeDefaultsDoc();
        applyMovementConfig(cfg.fallDamage);
        applyRepairKitPenalties(cfg.repairKits);
        applyGameplayConfig(cfg);

        this.config.save();
    }

    // -------------------------------------------------------------------------
    // Defaults documentation
    // -------------------------------------------------------------------------

    private void writeDefaultsDoc() {
        MovementConfig mc = MovementConfig.getAssetMap().getAsset("Default");
        GameplayConfig gc = GameplayConfig.getAssetMap().getAsset("Default");
        if (mc == null || gc == null) {
            LOGGER.atWarning().log("Could not read assets for defaults documentation");
            return;
        }

        var death    = gc.getDeathConfig();
        var dur      = gc.getItemDurabilityConfig().getBrokenPenalties();
        var world    = gc.getWorldConfig();
        var respawn  = gc.getRespawnConfig();
        var crafting = gc.getCraftingConfig();
        var combat   = gc.getCombatConfig();

        // Repair kit penalties from loaded suppliers (best-effort)
        double crudePenalty = getRepairKitPenalty("Tool_Repair_Kit_Crude");
        double ironPenalty  = getRepairKitPenalty("Tool_Repair_Kit_Iron");
        double rarePenalty  = getRepairKitPenalty("Tool_Repair_Kit_Rare");

        String doc = """
                // Configurable Everything — default / vanilla values
                // Copy any value into config.json to override it.
                // Setting an option back to null restores the default.
                {
                  "WorldGenV2": false,

                  "FallDamage": {
                    // Minimum fall speed (m/s) before a landing roll is attempted
                    "MinFallSpeedToEngageRoll":           %s,
                    // Maximum fall speed (m/s) that can still trigger a roll (above = no roll)
                    "MaxFallSpeedToEngageRoll":           %s,
                    // Percentage of fall damage negated by a successful roll (0–100)
                    "FallDamagePartialMitigationPercent": %s,
                    // Fall speeds at or below this value receive full damage mitigation from a roll
                    "MaxFallSpeedRollFullMitigation":     %s,
                    // Horizontal speed boost applied when the roll begins
                    "RollStartSpeedModifier":             %s,
                    // Horizontal speed at the end of the roll
                    "RollExitSpeedModifier":              %s,
                    // Duration of the roll animation in seconds
                    "RollTimeToComplete":                 %s
                  },

                  "RepairKits": {
                    // Primary-click max-durability penalty for the Crude Repair Kit (0 = no penalty)
                    "CrudePrimaryPenalty": %s,
                    // Primary-click max-durability penalty for the Iron Repair Kit
                    "IronPrimaryPenalty":  %s,
                    // Primary-click max-durability penalty for the Rare Repair Kit
                    "RarePrimaryPenalty":  %s
                  },

                  "Death": {
                    // Items-loss mode on death: "NONE", "ALL", or "CONFIGURED"
                    "ItemsLossMode":                "%s",
                    // Percentage of items lost from the inventory on death (only when CONFIGURED)
                    "ItemsAmountLossPercentage":    %s,
                    // Percentage of item durability lost on death
                    "ItemsDurabilityLossPercentage": %s
                  },

                  "ItemDurability": {
                    // Durability multiplier applied when a weapon breaks (0–1; lower = more loss)
                    "WeaponPenalty": %s,
                    // Durability multiplier applied when armor breaks
                    "ArmorPenalty":  %s,
                    // Durability multiplier applied when a tool breaks
                    "ToolPenalty":   %s
                  },

                  "World": {
                    // Real-world seconds for the daytime phase (sunrise → sunset)
                    "DaytimeDurationSeconds":   %s,
                    // Real-world seconds for the nighttime phase (sunset → sunrise)
                    "NighttimeDurationSeconds": %s,
                    // Whether players can break blocks
                    "AllowBlockBreaking":  %s,
                    // Whether players can gather (mine) blocks
                    "AllowBlockGathering": %s,
                    // Whether players can place blocks
                    "AllowBlockPlacement": %s
                  },

                  "Respawn": {
                    // Maximum distance (blocks) from spawn a player may set a respawn point
                    "RadiusLimitRespawnPoint":   %s,
                    // Maximum number of respawn points a player may have active
                    "MaxRespawnPointsPerPlayer": %s
                  },

                  "Crafting": {
                    // Horizontal block radius a crafting bench searches for chest materials (0–14)
                    "ChestHorizontalSearchRadius": %s,
                    // Vertical block radius a crafting bench searches for chest materials (0–14)
                    "ChestVerticalSearchRadius":   %s,
                    // Maximum number of chests a crafting bench will draw materials from (0–200)
                    "ChestLimit": %s
                  },

                  "Combat": {
                    // Show health bars above entities (clients can still override in their own settings)
                    "DisplayHealthBars":   %s,
                    // Show damage numbers on entities
                    "DisplayCombatText":   %s,
                    // Prevent NPCs from taking incoming damage
                    "DisableNPCDamage":    %s,
                    // Prevent players from taking incoming damage
                    "DisablePlayerDamage": %s
                  }
                }
                """.formatted(
                mc.getMinFallSpeedToEngageRoll(),
                mc.getMaxFallSpeedToEngageRoll(),
                mc.getFallDamagePartialMitigationPercent(),
                mc.getMaxFallSpeedRollFullMitigation(),
                mc.getRollStartSpeedModifier(),
                mc.getRollExitSpeedModifier(),
                mc.getRollTimeToComplete(),
                crudePenalty, ironPenalty, rarePenalty,
                death.getItemsLossMode().name().toLowerCase(Locale.ROOT),
                death.getItemsAmountLossPercentage(),
                death.getItemsDurabilityLossPercentage(),
                dur.getWeapon(0.75), dur.getArmor(0.75), dur.getTool(0.75),
                world.getDaytimeDurationSeconds(),
                world.getNighttimeDurationSeconds(),
                world.isBlockBreakingAllowed(),
                world.isBlockGatheringAllowed(),
                world.isBlockPlacementAllowed(),
                respawn.getRadiusLimitRespawnPoint(),
                respawn.getMaxRespawnPointsPerPlayer(),
                crafting.getBenchMaterialHorizontalChestSearchRadius(),
                crafting.getBenchMaterialVerticalChestSearchRadius(),
                crafting.getBenchMaterialChestLimit(),
                combat.isDisplayHealthBars(),
                combat.isDisplayCombatText(),
                combat.isNpcIncomingDamageDisabled(),
                combat.isPlayerIncomingDamageDisabled()
        );

        try {
            Files.createDirectories(this.getDataDirectory());
            Files.writeString(this.getDataDirectory().resolve("defaults.json"), doc);
        } catch (IOException e) {
            LOGGER.atWarning().log("Could not write defaults.json: %s", e.getMessage());
        }
    }

    private double getRepairKitPenalty(String itemId) {
        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) return Double.NaN;
        Map<InteractionType, String> interactions = getField(item, "interactions");
        if (interactions == null) return Double.NaN;
        String rootId = interactions.get(InteractionType.Primary);
        if (rootId == null) return Double.NaN;
        RootInteraction root = RootInteraction.getAssetMap().getAsset(rootId);
        if (root == null) return Double.NaN;
        String[] ids = getField(root, "interactionIds");
        if (ids == null) return Double.NaN;
        for (String id : ids) {
            Interaction interaction = Interaction.getAssetMap().getAsset(id);
            if (!(interaction instanceof OpenCustomUIInteraction openUI)) continue;
            Object supplierObj = getField(openUI, "customPageSupplier");
            if (!(supplierObj instanceof ItemRepairPageSupplier supplier)) continue;
            Double penalty = getField(supplier, "repairPenalty");
            return penalty != null ? penalty : Double.NaN;
        }
        return Double.NaN;
    }

    // -------------------------------------------------------------------------
    // WorldGen V2
    // -------------------------------------------------------------------------

    private void worldGenV2() {
        LOGGER.atInfo().log("Enabling World Gen V2");
        var V2 = IWorldGenProvider.CODEC.getCodecFor("HytaleGenerator");
        IWorldGenProvider.CODEC.remove(HandleProvider.class);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(2), "HytaleGenerator", HandleProvider.class, V2);
    }

    // -------------------------------------------------------------------------
    // Movement / fall damage
    // -------------------------------------------------------------------------

    private void applyMovementConfig(CEConfig.FallDamageConfig cfg) {
        if (cfg == null) return;
        MovementConfig mc = MovementConfig.getAssetMap().getAsset("Default");
        if (mc == null) {
            LOGGER.atWarning().log("Could not find 'Default' MovementConfig");
            return;
        }
        applyIfPresent(mc, "minFallSpeedToEngageRoll",           cfg.minFallSpeedToEngageRoll,           "MinFallSpeedToEngageRoll");
        applyIfPresent(mc, "maxFallSpeedToEngageRoll",           cfg.maxFallSpeedToEngageRoll,           "MaxFallSpeedToEngageRoll");
        applyIfPresent(mc, "fallDamagePartialMitigationPercent", cfg.fallDamagePartialMitigationPercent, "FallDamagePartialMitigationPercent");
        applyIfPresent(mc, "maxFallSpeedRollFullMitigation",     cfg.maxFallSpeedRollFullMitigation,     "MaxFallSpeedRollFullMitigation");
        applyIfPresent(mc, "rollStartSpeedModifier",             cfg.rollStartSpeedModifier,             "RollStartSpeedModifier");
        applyIfPresent(mc, "rollExitSpeedModifier",              cfg.rollExitSpeedModifier,              "RollExitSpeedModifier");
        applyIfPresent(mc, "rollTimeToComplete",                 cfg.rollTimeToComplete,                 "RollTimeToComplete");
    }

    // -------------------------------------------------------------------------
    // Repair kit penalties
    // -------------------------------------------------------------------------

    private void applyRepairKitPenalties(CEConfig.RepairKitConfig cfg) {
        if (cfg == null) return;
        cfg.crudePrimaryPenalty.ifPresent(p -> setRepairKitPenalty("Tool_Repair_Kit_Crude", p));
        cfg.ironPrimaryPenalty .ifPresent(p -> setRepairKitPenalty("Tool_Repair_Kit_Iron",  p));
        cfg.rarePrimaryPenalty .ifPresent(p -> setRepairKitPenalty("Tool_Repair_Kit_Rare",  p));
    }

    private void setRepairKitPenalty(String itemId, double penalty) {
        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) {
            LOGGER.atWarning().log("Could not find item '%s'", itemId);
            return;
        }
        Map<InteractionType, String> interactions = getField(item, "interactions");
        if (interactions == null) {
            LOGGER.atWarning().log("Could not read interactions from '%s'", itemId);
            return;
        }
        String rootId = interactions.get(InteractionType.Primary);
        if (rootId == null) {
            LOGGER.atWarning().log("No Primary interaction on '%s'", itemId);
            return;
        }
        RootInteraction root = RootInteraction.getAssetMap().getAsset(rootId);
        if (root == null) {
            LOGGER.atWarning().log("Could not find RootInteraction '%s' for '%s'", rootId, itemId);
            return;
        }
        String[] interactionIds = getField(root, "interactionIds");
        if (interactionIds == null) {
            LOGGER.atWarning().log("Could not read interactionIds from RootInteraction '%s'", rootId);
            return;
        }
        for (String id : interactionIds) {
            Interaction interaction = Interaction.getAssetMap().getAsset(id);
            if (!(interaction instanceof OpenCustomUIInteraction openUI)) continue;
            Object supplierObj = getField(openUI, "customPageSupplier");
            if (!(supplierObj instanceof ItemRepairPageSupplier supplier)) continue;
            setField(supplier, "repairPenalty", penalty);
            LOGGER.atInfo().log("Set repair penalty for '%s' Primary to %s", itemId, penalty);
            return;
        }
        LOGGER.atWarning().log("No repair page supplier found in Primary interaction of '%s'", itemId);
    }

    // -------------------------------------------------------------------------
    // GameplayConfig sub-objects
    // -------------------------------------------------------------------------

    private void applyGameplayConfig(CEConfig cfg) {
        GameplayConfig gc = GameplayConfig.getAssetMap().getAsset("Default");
        if (gc == null) {
            LOGGER.atWarning().log("Could not find 'Default' GameplayConfig");
            return;
        }
        applyDeathConfig(cfg.death, gc);
        applyDurabilityConfig(cfg.durability, gc);
        applyWorldConfig(cfg.world, gc);
        applyRespawnConfig(cfg.respawn, gc);
        applyCraftingConfig(cfg.crafting, gc);
        applyCombatConfig(cfg.combat, gc);
    }

    private void applyDeathConfig(CEConfig.DeathCfg cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object dc = getField(gc, "deathConfig");
        if (dc == null) return;
        applyIfPresent(dc, "itemsAmountLossPercentage",   cfg.itemsAmountLossPercentage,   "DeathConfig.ItemsAmountLossPercentage");
        applyIfPresent(dc, "itemsDurabilityLossPercentage", cfg.itemsDurabilityLossPercentage, "DeathConfig.ItemsDurabilityLossPercentage");
        cfg.itemsLossMode.ifPresent(mode -> {
            try {
                setField(dc, "itemsLossMode", DeathConfig.ItemsLossMode.valueOf(mode.toUpperCase()));
                LOGGER.atInfo().log("Set DeathConfig.ItemsLossMode to %s", mode);
            } catch (IllegalArgumentException e) {
                LOGGER.atWarning().log("Unknown ItemsLossMode '%s' — valid: NONE, ALL, CONFIGURED", mode);
            }
        });
    }

    private void applyDurabilityConfig(CEConfig.DurabilityCfg cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object idc = getField(gc, "itemDurabilityConfig");
        if (idc == null) return;
        Object bp = getField(idc, "brokenPenalties");
        if (bp == null) return;
        applyIfPresent(bp, "weapon", cfg.weaponPenalty, "BrokenPenalties.weapon");
        applyIfPresent(bp, "armor",  cfg.armorPenalty,  "BrokenPenalties.armor");
        applyIfPresent(bp, "tool",   cfg.toolPenalty,   "BrokenPenalties.tool");
    }

    private void applyWorldConfig(CEConfig.WorldCfg cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object wc = getField(gc, "worldConfig");
        if (wc == null) return;
        applyIfPresent(wc, "daytimeDurationSeconds",   cfg.daytimeDurationSeconds,   "WorldConfig.DaytimeDurationSeconds");
        applyIfPresent(wc, "nighttimeDurationSeconds", cfg.nighttimeDurationSeconds, "WorldConfig.NighttimeDurationSeconds");
        applyIfPresent(wc, "allowBlockBreaking",       cfg.allowBlockBreaking,       "WorldConfig.AllowBlockBreaking");
        applyIfPresent(wc, "allowBlockGathering",      cfg.allowBlockGathering,      "WorldConfig.AllowBlockGathering");
        applyIfPresent(wc, "allowBlockPlacement",      cfg.allowBlockPlacement,      "WorldConfig.AllowBlockPlacement");
    }

    private void applyRespawnConfig(CEConfig.RespawnCfg cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object rc = getField(gc, "respawnConfig");
        if (rc == null) return;
        applyIfPresent(rc, "radiusLimitRespawnPoint",   cfg.radiusLimitRespawnPoint,   "RespawnConfig.RadiusLimitRespawnPoint");
        applyIfPresent(rc, "maxRespawnPointsPerPlayer", cfg.maxRespawnPointsPerPlayer, "RespawnConfig.MaxRespawnPointsPerPlayer");
    }

    private void applyCraftingConfig(CEConfig.CraftingCfg cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object cc = getField(gc, "craftingConfig");
        if (cc == null) return;
        applyIfPresent(cc, "benchMaterialHorizontalChestSearchRadius", cfg.chestHorizontalSearchRadius, "CraftingConfig.ChestHorizontalSearchRadius");
        applyIfPresent(cc, "benchMaterialVerticalChestSearchRadius",   cfg.chestVerticalSearchRadius,   "CraftingConfig.ChestVerticalSearchRadius");
        applyIfPresent(cc, "benchMaterialChestLimit",                  cfg.chestLimit,                  "CraftingConfig.ChestLimit");
    }

    private void applyCombatConfig(CEConfig.CombatCfg cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object cc = getField(gc, "combatConfig");
        if (cc == null) return;
        applyIfPresent(cc, "displayHealthBars",           cfg.displayHealthBars,   "CombatConfig.DisplayHealthBars");
        applyIfPresent(cc, "displayCombatText",           cfg.displayCombatText,   "CombatConfig.DisplayCombatText");
        applyIfPresent(cc, "disableNpcIncomingDamage",    cfg.disableNpcDamage,    "CombatConfig.DisableNPCDamage");
        applyIfPresent(cc, "disablePlayerIncomingDamage", cfg.disablePlayerDamage, "CombatConfig.DisablePlayerDamage");
    }

    // -------------------------------------------------------------------------
    // Reflection helpers
    // -------------------------------------------------------------------------

    private static <T> void applyIfPresent(Object target, String fieldName, Optional<T> value, String logName) {
        if (value == null || value.isEmpty()) return;
        setField(target, fieldName, value.get());
        LOGGER.atInfo().log("Set %s to %s", logName, value.get());
    }

    /** Converts a null Optional (set directly by BuilderField when JSON value is null) back to empty. */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Optional<T> opt(@Nullable Optional<T> v) {
        return v != null ? v : Optional.empty();
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                LOGGER.atWarning().log("Field '%s' not found on %s", fieldName, target.getClass().getSimpleName());
                return;
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to set '%s' on %s: %s", fieldName, target.getClass().getSimpleName(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T getField(Object target, String fieldName) {
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                LOGGER.atWarning().log("Field '%s' not found on %s", fieldName, target.getClass().getSimpleName());
                return null;
            }
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to get '%s' from %s: %s", fieldName, target.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private static @Nullable Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    // =========================================================================
    // Config
    // =========================================================================

    public static class CEConfig {

        public static final BuilderCodec<CEConfig> CODEC = BuilderCodec.builder(CEConfig.class, CEConfig::new)
            .append(new KeyedCodec<>("WorldGenV2",    Codec.BOOLEAN),                             (c, v) -> c.worldGenV2 = v, c -> c.worldGenV2).add()
            .append(new KeyedCodec<>("FallDamage",    new NullableCodec<>(FallDamageConfig.CODEC)), (c, v) -> c.fallDamage = v, c -> c.fallDamage).add()
            .append(new KeyedCodec<>("RepairKits",    new NullableCodec<>(RepairKitConfig.CODEC)),  (c, v) -> c.repairKits  = v, c -> c.repairKits).add()
            .append(new KeyedCodec<>("Death",         new NullableCodec<>(DeathCfg.CODEC)),         (c, v) -> c.death       = v, c -> c.death).add()
            .append(new KeyedCodec<>("ItemDurability", new NullableCodec<>(DurabilityCfg.CODEC)),   (c, v) -> c.durability  = v, c -> c.durability).add()
            .append(new KeyedCodec<>("World",         new NullableCodec<>(WorldCfg.CODEC)),         (c, v) -> c.world       = v, c -> c.world).add()
            .append(new KeyedCodec<>("Respawn",       new NullableCodec<>(RespawnCfg.CODEC)),       (c, v) -> c.respawn     = v, c -> c.respawn).add()
            .append(new KeyedCodec<>("Crafting",      new NullableCodec<>(CraftingCfg.CODEC)),      (c, v) -> c.crafting    = v, c -> c.crafting).add()
            .append(new KeyedCodec<>("Combat",        new NullableCodec<>(CombatCfg.CODEC)),        (c, v) -> c.combat      = v, c -> c.combat).add()
            .build();

        public boolean worldGenV2 = false;
        // Sections are non-null by default so they are included in the saved config.
        // A section set to null in JSON is decoded as null and all its options are skipped.
        public FallDamageConfig fallDamage = new FallDamageConfig();
        public RepairKitConfig  repairKits  = new RepairKitConfig();
        public DeathCfg         death       = new DeathCfg();
        public DurabilityCfg    durability  = new DurabilityCfg();
        public WorldCfg         world       = new WorldCfg();
        public RespawnCfg       respawn     = new RespawnCfg();
        public CraftingCfg      crafting    = new CraftingCfg();
        public CombatCfg        combat      = new CombatCfg();

        // ----- FallDamage -----

        public static class FallDamageConfig {
            private static final OptionalCodec<Float> CVF = new OptionalCodec<>(Codec.FLOAT);

            public static final BuilderCodec<FallDamageConfig> CODEC = BuilderCodec.builder(FallDamageConfig.class, FallDamageConfig::new)
                .append(new KeyedCodec<>("MinFallSpeedToEngageRoll",           CVF), (c, v) -> c.minFallSpeedToEngageRoll           = CEPlugin.opt(v), c -> c.minFallSpeedToEngageRoll).add()
                .append(new KeyedCodec<>("MaxFallSpeedToEngageRoll",           CVF), (c, v) -> c.maxFallSpeedToEngageRoll           = CEPlugin.opt(v), c -> c.maxFallSpeedToEngageRoll).add()
                .append(new KeyedCodec<>("FallDamagePartialMitigationPercent", CVF), (c, v) -> c.fallDamagePartialMitigationPercent = CEPlugin.opt(v), c -> c.fallDamagePartialMitigationPercent).add()
                .append(new KeyedCodec<>("MaxFallSpeedRollFullMitigation",     CVF), (c, v) -> c.maxFallSpeedRollFullMitigation     = CEPlugin.opt(v), c -> c.maxFallSpeedRollFullMitigation).add()
                .append(new KeyedCodec<>("RollStartSpeedModifier",             CVF), (c, v) -> c.rollStartSpeedModifier             = CEPlugin.opt(v), c -> c.rollStartSpeedModifier).add()
                .append(new KeyedCodec<>("RollExitSpeedModifier",              CVF), (c, v) -> c.rollExitSpeedModifier              = CEPlugin.opt(v), c -> c.rollExitSpeedModifier).add()
                .append(new KeyedCodec<>("RollTimeToComplete",                 CVF), (c, v) -> c.rollTimeToComplete                 = CEPlugin.opt(v), c -> c.rollTimeToComplete).add()
                .build();

            public Optional<Float> minFallSpeedToEngageRoll           = Optional.empty();
            public Optional<Float> maxFallSpeedToEngageRoll           = Optional.empty();
            public Optional<Float> fallDamagePartialMitigationPercent = Optional.empty();
            public Optional<Float> maxFallSpeedRollFullMitigation     = Optional.empty();
            public Optional<Float> rollStartSpeedModifier             = Optional.empty();
            public Optional<Float> rollExitSpeedModifier              = Optional.empty();
            public Optional<Float> rollTimeToComplete                 = Optional.empty();
        }

        // ----- RepairKits -----

        public static class RepairKitConfig {
            private static final OptionalCodec<Double> CVD = new OptionalCodec<>(Codec.DOUBLE);

            public static final BuilderCodec<RepairKitConfig> CODEC = BuilderCodec.builder(RepairKitConfig.class, RepairKitConfig::new)
                .append(new KeyedCodec<>("CrudePrimaryPenalty", CVD), (c, v) -> c.crudePrimaryPenalty = CEPlugin.opt(v), c -> c.crudePrimaryPenalty).add()
                .append(new KeyedCodec<>("IronPrimaryPenalty",  CVD), (c, v) -> c.ironPrimaryPenalty  = CEPlugin.opt(v), c -> c.ironPrimaryPenalty).add()
                .append(new KeyedCodec<>("RarePrimaryPenalty",  CVD), (c, v) -> c.rarePrimaryPenalty  = CEPlugin.opt(v), c -> c.rarePrimaryPenalty).add()
                .build();

            public Optional<Double> crudePrimaryPenalty = Optional.empty();
            public Optional<Double> ironPrimaryPenalty  = Optional.empty();
            public Optional<Double> rarePrimaryPenalty  = Optional.empty();
        }

        // ----- Death -----

        public static class DeathCfg {
            private static final OptionalCodec<Double> CVD = new OptionalCodec<>(Codec.DOUBLE);
            private static final OptionalCodec<String> CVS = new OptionalCodec<>(Codec.STRING);

            public static final BuilderCodec<DeathCfg> CODEC = BuilderCodec.builder(DeathCfg.class, DeathCfg::new)
                .append(new KeyedCodec<>("ItemsLossMode",                CVS), (c, v) -> c.itemsLossMode               = CEPlugin.opt(v), c -> c.itemsLossMode).add()
                .append(new KeyedCodec<>("ItemsAmountLossPercentage",    CVD), (c, v) -> c.itemsAmountLossPercentage   = CEPlugin.opt(v), c -> c.itemsAmountLossPercentage).add()
                .append(new KeyedCodec<>("ItemsDurabilityLossPercentage", CVD), (c, v) -> c.itemsDurabilityLossPercentage = CEPlugin.opt(v), c -> c.itemsDurabilityLossPercentage).add()
                .build();

            /** "NONE" | "ALL" | "CONFIGURED" */
            public Optional<String> itemsLossMode                 = Optional.empty();
            public Optional<Double> itemsAmountLossPercentage     = Optional.empty();
            public Optional<Double> itemsDurabilityLossPercentage = Optional.empty();
        }

        // ----- ItemDurability -----

        public static class DurabilityCfg {
            private static final OptionalCodec<Double> CVD = new OptionalCodec<>(Codec.DOUBLE);

            public static final BuilderCodec<DurabilityCfg> CODEC = BuilderCodec.builder(DurabilityCfg.class, DurabilityCfg::new)
                .append(new KeyedCodec<>("WeaponPenalty", CVD), (c, v) -> c.weaponPenalty = CEPlugin.opt(v), c -> c.weaponPenalty).add()
                .append(new KeyedCodec<>("ArmorPenalty",  CVD), (c, v) -> c.armorPenalty  = CEPlugin.opt(v), c -> c.armorPenalty).add()
                .append(new KeyedCodec<>("ToolPenalty",   CVD), (c, v) -> c.toolPenalty   = CEPlugin.opt(v), c -> c.toolPenalty).add()
                .build();

            public Optional<Double> weaponPenalty = Optional.empty();
            public Optional<Double> armorPenalty  = Optional.empty();
            public Optional<Double> toolPenalty   = Optional.empty();
        }

        // ----- World -----

        public static class WorldCfg {
            private static final OptionalCodec<Integer> CVI = new OptionalCodec<>(Codec.INTEGER);
            private static final OptionalCodec<Boolean> CVB = new OptionalCodec<>(Codec.BOOLEAN);

            public static final BuilderCodec<WorldCfg> CODEC = BuilderCodec.builder(WorldCfg.class, WorldCfg::new)
                .append(new KeyedCodec<>("DaytimeDurationSeconds",   CVI), (c, v) -> c.daytimeDurationSeconds   = CEPlugin.opt(v), c -> c.daytimeDurationSeconds).add()
                .append(new KeyedCodec<>("NighttimeDurationSeconds", CVI), (c, v) -> c.nighttimeDurationSeconds = CEPlugin.opt(v), c -> c.nighttimeDurationSeconds).add()
                .append(new KeyedCodec<>("AllowBlockBreaking",       CVB), (c, v) -> c.allowBlockBreaking       = CEPlugin.opt(v), c -> c.allowBlockBreaking).add()
                .append(new KeyedCodec<>("AllowBlockGathering",      CVB), (c, v) -> c.allowBlockGathering      = CEPlugin.opt(v), c -> c.allowBlockGathering).add()
                .append(new KeyedCodec<>("AllowBlockPlacement",      CVB), (c, v) -> c.allowBlockPlacement      = CEPlugin.opt(v), c -> c.allowBlockPlacement).add()
                .build();

            public Optional<Integer> daytimeDurationSeconds   = Optional.empty();
            public Optional<Integer> nighttimeDurationSeconds = Optional.empty();
            public Optional<Boolean> allowBlockBreaking       = Optional.empty();
            public Optional<Boolean> allowBlockGathering      = Optional.empty();
            public Optional<Boolean> allowBlockPlacement      = Optional.empty();
        }

        // ----- Respawn -----

        public static class RespawnCfg {
            private static final OptionalCodec<Integer> CVI = new OptionalCodec<>(Codec.INTEGER);

            public static final BuilderCodec<RespawnCfg> CODEC = BuilderCodec.builder(RespawnCfg.class, RespawnCfg::new)
                .append(new KeyedCodec<>("RadiusLimitRespawnPoint",   CVI), (c, v) -> c.radiusLimitRespawnPoint   = CEPlugin.opt(v), c -> c.radiusLimitRespawnPoint).add()
                .append(new KeyedCodec<>("MaxRespawnPointsPerPlayer", CVI), (c, v) -> c.maxRespawnPointsPerPlayer = CEPlugin.opt(v), c -> c.maxRespawnPointsPerPlayer).add()
                .build();

            public Optional<Integer> radiusLimitRespawnPoint   = Optional.empty();
            public Optional<Integer> maxRespawnPointsPerPlayer = Optional.empty();
        }

        // ----- Crafting -----

        public static class CraftingCfg {
            private static final OptionalCodec<Integer> CVI = new OptionalCodec<>(Codec.INTEGER);

            public static final BuilderCodec<CraftingCfg> CODEC = BuilderCodec.builder(CraftingCfg.class, CraftingCfg::new)
                .append(new KeyedCodec<>("ChestHorizontalSearchRadius", CVI), (c, v) -> c.chestHorizontalSearchRadius = CEPlugin.opt(v), c -> c.chestHorizontalSearchRadius).add()
                .append(new KeyedCodec<>("ChestVerticalSearchRadius",   CVI), (c, v) -> c.chestVerticalSearchRadius   = CEPlugin.opt(v), c -> c.chestVerticalSearchRadius).add()
                .append(new KeyedCodec<>("ChestLimit",                  CVI), (c, v) -> c.chestLimit                  = CEPlugin.opt(v), c -> c.chestLimit).add()
                .build();

            public Optional<Integer> chestHorizontalSearchRadius = Optional.empty();
            public Optional<Integer> chestVerticalSearchRadius   = Optional.empty();
            public Optional<Integer> chestLimit                  = Optional.empty();
        }

        // ----- Combat -----

        public static class CombatCfg {
            private static final OptionalCodec<Boolean> CVB = new OptionalCodec<>(Codec.BOOLEAN);

            public static final BuilderCodec<CombatCfg> CODEC = BuilderCodec.builder(CombatCfg.class, CombatCfg::new)
                .append(new KeyedCodec<>("DisplayHealthBars",   CVB), (c, v) -> c.displayHealthBars   = CEPlugin.opt(v), c -> c.displayHealthBars).add()
                .append(new KeyedCodec<>("DisplayCombatText",   CVB), (c, v) -> c.displayCombatText   = CEPlugin.opt(v), c -> c.displayCombatText).add()
                .append(new KeyedCodec<>("DisableNPCDamage",    CVB), (c, v) -> c.disableNpcDamage    = CEPlugin.opt(v), c -> c.disableNpcDamage).add()
                .append(new KeyedCodec<>("DisablePlayerDamage", CVB), (c, v) -> c.disablePlayerDamage = CEPlugin.opt(v), c -> c.disablePlayerDamage).add()
                .build();

            public Optional<Boolean> displayHealthBars   = Optional.empty();
            public Optional<Boolean> displayCombatText   = Optional.empty();
            public Optional<Boolean> disableNpcDamage    = Optional.empty();
            public Optional<Boolean> disablePlayerDamage = Optional.empty();
        }
    }
}
