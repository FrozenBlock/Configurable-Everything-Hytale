package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.suppliers.ItemRepairPageSupplier;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Map;
import java.util.Optional;

public class CERepairKitConfig {
    public static final BuilderCodec<CERepairKitConfig> CODEC = BuilderCodec.builder(CERepairKitConfig.class, CERepairKitConfig::new)
        .append(new KeyedCodec<>("CrudePrimaryPenalty", OptionalCodec.DOUBLE), (c, v) -> c.crudePrimaryPenalty = CEPlugin.opt(v), c -> c.crudePrimaryPenalty).add()
        .append(new KeyedCodec<>("IronPrimaryPenalty", OptionalCodec.DOUBLE), (c, v) -> c.ironPrimaryPenalty = CEPlugin.opt(v), c -> c.ironPrimaryPenalty).add()
        .append(new KeyedCodec<>("RarePrimaryPenalty", OptionalCodec.DOUBLE), (c, v) -> c.rarePrimaryPenalty = CEPlugin.opt(v), c -> c.rarePrimaryPenalty).add()
        .build();

    public Optional<Double> crudePrimaryPenalty = Optional.empty();
    public Optional<Double> ironPrimaryPenalty = Optional.empty();
    public Optional<Double> rarePrimaryPenalty = Optional.empty();

    public static void apply(CERepairKitConfig cfg) {
        if (cfg == null) return;
        cfg.crudePrimaryPenalty.ifPresent(p -> applyToItem("Tool_Repair_Kit_Crude", p));
        cfg.ironPrimaryPenalty .ifPresent(p -> applyToItem("Tool_Repair_Kit_Iron",  p));
        cfg.rarePrimaryPenalty .ifPresent(p -> applyToItem("Tool_Repair_Kit_Rare",  p));
    }

    private static void applyToItem(String itemId, double penalty) {
        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) {
            CEPlugin.LOGGER.atWarning().log("Could not find item '%s'", itemId);
            return;
        }
        Map<InteractionType, String> interactions = CEPlugin.getField(item, "interactions");
        if (interactions == null) {
            CEPlugin.LOGGER.atWarning().log("Could not read interactions from '%s'", itemId);
            return;
        }
        String rootId = interactions.get(InteractionType.Primary);
        if (rootId == null) {
            CEPlugin.LOGGER.atWarning().log("No Primary interaction on '%s'", itemId);
            return;
        }
        RootInteraction root = RootInteraction.getAssetMap().getAsset(rootId);
        if (root == null) {
            CEPlugin.LOGGER.atWarning().log("Could not find RootInteraction '%s' for '%s'", rootId, itemId);
            return;
        }
        String[] interactionIds = CEPlugin.getField(root, "interactionIds");
        if (interactionIds == null) {
            CEPlugin.LOGGER.atWarning().log("Could not read interactionIds from RootInteraction '%s'", rootId);
            return;
        }
        for (String id : interactionIds) {
            Interaction interaction = Interaction.getAssetMap().getAsset(id);
            if (!(interaction instanceof OpenCustomUIInteraction openUI)) continue;
            Object supplierObj = CEPlugin.getField(openUI, "customPageSupplier");
            if (!(supplierObj instanceof ItemRepairPageSupplier supplier)) continue;
            CEPlugin.setField(supplier, "repairPenalty", penalty);
            CEPlugin.LOGGER.atInfo().log("Set repair penalty for '%s' Primary to %s", itemId, penalty);
            return;
        }
        CEPlugin.LOGGER.atWarning().log("No repair page supplier found in Primary interaction of '%s'", itemId);
    }
}
