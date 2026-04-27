package net.frozenblock.configurableeverything;

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.suppliers.ItemRepairPageSupplier;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.util.Config;
import net.frozenblock.configurableeverything.config.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
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
        CEConfig.writeDefaultsDoc(this.getDataDirectory());
        CEFallDamageConfig.apply(cfg.fallDamage);
        CERepairKitConfig.apply(cfg.repairKits);
        applyGameplayConfig(cfg);

        this.config.save();
    }

    // -------------------------------------------------------------------------
    // Defaults documentation
    // -------------------------------------------------------------------------

    public static double getRepairKitPenalty(String itemId) {
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

    private static void applyGameplayConfig(CEConfig cfg) {
        GameplayConfig gc = GameplayConfig.getAssetMap().getAsset("Default");
        if (gc == null) {
            LOGGER.atWarning().log("Could not find 'Default' GameplayConfig");
            return;
        }
        CEDeathConfig.apply(cfg.death, gc);
        CEDurabilityConfig.apply(cfg.durability, gc);
        CEWorldConfig.apply(cfg.world, gc);
        CERespawnConfig.apply(cfg.respawn, gc);
        CECraftingConfig.apply(cfg.crafting, gc);
        CECombatConfig.apply(cfg.combat, gc);
    }

    // -------------------------------------------------------------------------
    // Reflection helpers
    // -------------------------------------------------------------------------

    public static <T> void applyIfPresent(Object target, String fieldName, Optional<T> value, String logName) {
        if (value == null || value.isEmpty()) return;
        setField(target, fieldName, value.get());
        LOGGER.atInfo().log("Set %s to %s", logName, value.get());
    }

    /** Converts a null Optional (set directly by BuilderField when JSON value is null) back to empty. */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Optional<T> opt(@Nullable Optional<T> v) {
        return v != null ? v : Optional.empty();
    }

    public static void setField(Object target, String fieldName, Object value) {
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
    public static <T> @Nullable T getField(Object target, String fieldName) {
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

    public static @Nullable Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
