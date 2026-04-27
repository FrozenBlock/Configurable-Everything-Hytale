package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.NullableCodec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class CEConfig {
    public static final BuilderCodec<CEConfig> CODEC = BuilderCodec.builder(CEConfig.class, CEConfig::new)
        .append(new KeyedCodec<>("WorldGenV2", Codec.BOOLEAN), (c, v) -> c.worldGenV2 = v, c -> c.worldGenV2).add()
        .append(new KeyedCodec<>("FallDamage", new NullableCodec<>(CEFallDamageConfig.CODEC)), (c, v) -> c.fallDamage = v, c -> c.fallDamage).add()
        .append(new KeyedCodec<>("RepairKits", new NullableCodec<>(CERepairKitConfig.CODEC)), (c, v) -> c.repairKits = v, c -> c.repairKits).add()
        .append(new KeyedCodec<>("Death", new NullableCodec<>(CEDeathConfig.CODEC)), (c, v) -> c.death = v, c -> c.death).add()
        .append(new KeyedCodec<>("ItemDurability", new NullableCodec<>(CEDurabilityConfig.CODEC)), (c, v) -> c.durability  = v, c -> c.durability).add()
        .append(new KeyedCodec<>("World", new NullableCodec<>(CEWorldConfig.CODEC)), (c, v) -> c.world = v, c -> c.world).add()
        .append(new KeyedCodec<>("Respawn", new NullableCodec<>(CERespawnConfig.CODEC)), (c, v) -> c.respawn = v, c -> c.respawn).add()
        .append(new KeyedCodec<>("Crafting", new NullableCodec<>(CECraftingConfig.CODEC)), (c, v) -> c.crafting = v, c -> c.crafting).add()
        .append(new KeyedCodec<>("Combat", new NullableCodec<>(CECombatConfig.CODEC)), (c, v) -> c.combat = v, c -> c.combat).add()
        .build();

    public boolean worldGenV2 = false;
    public CEFallDamageConfig fallDamage = new CEFallDamageConfig();
    public CERepairKitConfig repairKits = new CERepairKitConfig();
    public CEDeathConfig death = new CEDeathConfig();
    public CEDurabilityConfig durability = new CEDurabilityConfig();
    public CEWorldConfig world = new CEWorldConfig();
    public CERespawnConfig respawn = new CERespawnConfig();
    public CECraftingConfig crafting = new CECraftingConfig();
    public CECombatConfig combat = new CECombatConfig();

    public static void writeDefaultsDoc(Path dataDirectory) {
        MovementConfig mc = MovementConfig.getAssetMap().getAsset("Default");
        GameplayConfig gc = GameplayConfig.getAssetMap().getAsset("Default");
        if (mc == null || gc == null) {
            CEPlugin.LOGGER.atWarning().log("Could not read assets for defaults documentation");
            return;
        }

        var death = gc.getDeathConfig();
        var dur = gc.getItemDurabilityConfig().getBrokenPenalties();
        var world = gc.getWorldConfig();
        var respawn = gc.getRespawnConfig();
        var crafting = gc.getCraftingConfig();
        var combat = gc.getCombatConfig();

        // Repair kit penalties from loaded suppliers (best-effort)
        double crudePenalty = CEPlugin.getRepairKitPenalty("Tool_Repair_Kit_Crude");
        double ironPenalty = CEPlugin.getRepairKitPenalty("Tool_Repair_Kit_Iron");
        double rarePenalty = CEPlugin.getRepairKitPenalty("Tool_Repair_Kit_Rare");

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
                     "ChestLimit": %s,
                     // Modify crafting recipes: add/remove/edit ingredients
                     "RecipeModifications": [
                       {
                         // EXAMPLE, DEFAULT IS NO MODIFICATIONS
                         "RecipeId": "Tool_Repair_Kit_Iron_Recipe_Generated_0",
                         "Ingredients": [
                           {
                             "ItemId": "Ingredient_Bar_Iron",
                             "Quantity": 1,
                             "Remove": false
                           }
                         ]
                       }
                     ]
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
            Files.createDirectories(dataDirectory);
            Files.writeString(dataDirectory.resolve("defaults.json"), doc);
        } catch (IOException e) {
            CEPlugin.LOGGER.atWarning().log("Could not write defaults.json: %s", e.getMessage());
        }
    }
}
