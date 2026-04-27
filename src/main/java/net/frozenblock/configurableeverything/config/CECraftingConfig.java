package net.frozenblock.configurableeverything.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;
import net.frozenblock.configurableeverything.config.recipe.IngredientModification;
import net.frozenblock.configurableeverything.config.recipe.RecipeModification;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CECraftingConfig {
    public static final BuilderCodec<CECraftingConfig> CODEC = BuilderCodec.builder(CECraftingConfig.class, CECraftingConfig::new)
        .append(new KeyedCodec<>("ChestHorizontalSearchRadius", OptionalCodec.INTEGER), (c, v) -> c.chestHorizontalSearchRadius = CEPlugin.opt(v), c -> c.chestHorizontalSearchRadius)
        .documentation("Horizontal block radius a crafting bench searches for chest materials (0–14)")
        .add()
        .append(new KeyedCodec<>("ChestVerticalSearchRadius", OptionalCodec.INTEGER), (c, v) -> c.chestVerticalSearchRadius = CEPlugin.opt(v), c -> c.chestVerticalSearchRadius)
        .documentation("Vertical block radius a crafting bench searches for chest materials (0–14)")
        .add()
        .append(new KeyedCodec<>("ChestLimit", OptionalCodec.INTEGER), (c, v) -> c.chestLimit = CEPlugin.opt(v), c -> c.chestLimit)
        .documentation("Maximum number of chests a crafting bench will draw materials from (0–200)")
        .add()
        .append(new KeyedCodec<>("RecipeModifications", new OptionalCodec<>(new ArrayCodec<>(RecipeModification.CODEC, RecipeModification[]::new))),
            (c, v) -> c.recipeModifications = v,
            c -> c.recipeModifications
        )
        .documentation("Modify crafting recipes: add/remove/edit ingredients")
        .add()
        .build();

    public Optional<Integer> chestHorizontalSearchRadius = Optional.empty();
    public Optional<Integer> chestVerticalSearchRadius = Optional.empty();
    public Optional<Integer> chestLimit = Optional.empty();
    public Optional<RecipeModification[]> recipeModifications = Optional.empty();

    public static void apply(CECraftingConfig cfg, GameplayConfig gc) {
        if (cfg == null) return;
        Object cc = CEPlugin.getField(gc, "craftingConfig");
        if (cc == null) return;
        CEPlugin.applyIfPresent(cc, "benchMaterialHorizontalChestSearchRadius", cfg.chestHorizontalSearchRadius, "CraftingConfig.ChestHorizontalSearchRadius");
        CEPlugin.applyIfPresent(cc, "benchMaterialVerticalChestSearchRadius", cfg.chestVerticalSearchRadius, "CraftingConfig.ChestVerticalSearchRadius");
        CEPlugin.applyIfPresent(cc, "benchMaterialChestLimit", cfg.chestLimit, "CraftingConfig.ChestLimit");

        // Apply recipe modifications
        cfg.recipeModifications.ifPresent(CECraftingConfig::applyRecipeModifications);
    }

    private static void applyRecipeModifications(RecipeModification[] modifications) {
        for (Map.Entry<String, CraftingRecipe> poop :CraftingRecipe.getAssetMap().getAssetMap().entrySet()) {
            CEPlugin.LOGGER.atFine().log(poop.getKey());
        }
        for (RecipeModification mod : modifications) {
            if (mod.recipeId.isEmpty()) continue;
            String recipeId = mod.recipeId.get();

            CraftingRecipe recipe = CraftingRecipe.getAssetMap().getAsset(recipeId);
            if (recipe == null) {
                CEPlugin.LOGGER.atWarning().log("Could not find recipe '%s' for modification", recipeId);
                continue;
            }

            applyModificationToRecipe(recipe, mod.ingredients, recipeId);
        }
    }

    private static void applyModificationToRecipe(@NonNull CraftingRecipe recipe, Optional<IngredientModification>[] mods, String recipeId) {
        MaterialQuantity[] input = CEPlugin.getField(recipe, "input");
        if (input == null) {
            CEPlugin.LOGGER.atWarning().log("Could not read Input from recipe '%s'", recipeId);
            return;
        }

        List<MaterialQuantity> newInput = new ArrayList<>();
        for (MaterialQuantity mat : input) {
            newInput.add(mat);
        }

        // Process modifications
        for (Optional<IngredientModification> ingredMod : mods) {
            if (!ingredMod.isPresent()) continue;
            var ingred = ingredMod.get();
            if (ingred.remove) {
                // Remove matching ingredient
                newInput.removeIf(mat -> matchesIngredient(mat, ingred));
                CEPlugin.LOGGER.atInfo().log("Removed ingredient from recipe '%s'", recipeId);
            } else {
                // Add or update ingredient
                boolean found = false;
                for (MaterialQuantity mat : newInput) {
                    if (matchesIngredient(mat, ingred)) {
                        if (ingred.quantity.isPresent()) {
                            CEPlugin.setField(mat, "quantity", ingred.quantity.get());
                            CEPlugin.LOGGER.atInfo().log("Modified ingredient quantity in recipe '%s'", recipeId);
                        }
                        found = true;
                        break;
                    }
                }

                if (!found && ingred.quantity.isPresent()) {
                    // Add new ingredient
                    MaterialQuantity newMat = createMaterialQuantity(ingred);
                    if (newMat != null) {
                        newInput.add(newMat);
                        CEPlugin.LOGGER.atInfo().log("Added new ingredient to recipe '%s'", recipeId);
                    }
                }
            }
        }

        // Apply the modified input back to the recipe
        CEPlugin.setField(recipe, "input", newInput.toArray(new MaterialQuantity[0]));
    }

    private static boolean matchesIngredient(@NonNull MaterialQuantity mat, @NonNull IngredientModification mod) {
        if (mod.itemId.isPresent()) {
            String matItemId = CEPlugin.getField(mat, "itemId");
            if (matItemId != null && matItemId.equals(mod.itemId.get())) return true;
        }
        if (mod.resourceTypeId.isPresent()) {
            String matResourceTypeId = CEPlugin.getField(mat, "resourceTypeId");
            if (matResourceTypeId != null && matResourceTypeId.equals(mod.resourceTypeId.get())) return true;
        }
        if (mod.itemTag.isPresent()) {
            String matItemTag = CEPlugin.getField(mat, "tag");
            if (matItemTag != null && matItemTag.equals(mod.itemTag.get())) return true;
        }
        return false;
    }

    private static @Nullable MaterialQuantity createMaterialQuantity(@NonNull IngredientModification mod) {
        MaterialQuantity mat;
        try {
            // Use reflection to instantiate MaterialQuantity via protected constructor
            var constructor = MaterialQuantity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            mat = constructor.newInstance();
        } catch (Exception e) {
            CEPlugin.LOGGER.atWarning().log("Failed to create MaterialQuantity: %s", e.getMessage());
            return null;
        }

        mod.itemId.ifPresent(itemId -> CEPlugin.setField(mat, "itemId", itemId));
        mod.resourceTypeId.ifPresent(resourceTypeId -> CEPlugin.setField(mat, "resourceTypeId", resourceTypeId));
        mod.itemTag.ifPresent(itemTag -> CEPlugin.setField(mat, "itemTag", itemTag));
        if (mod.quantity.isPresent()) {
            CEPlugin.setField(mat, "quantity", mod.quantity.get());
        } else {
            CEPlugin.setField(mat, "quantity", 1);
        }
        return mat;
    }
}
