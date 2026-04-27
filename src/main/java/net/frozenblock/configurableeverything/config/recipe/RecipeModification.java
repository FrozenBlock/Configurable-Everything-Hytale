package net.frozenblock.configurableeverything.config.recipe;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

/**
 * Represents a modification to a specific crafting recipe.
 * Allows adding, modifying, or removing ingredients from a recipe.
 */
@SuppressWarnings("unchecked")
public class RecipeModification {
    public static final BuilderCodec<RecipeModification> CODEC = BuilderCodec.builder(RecipeModification.class, RecipeModification::new)
        .documentation("The ID of the recipe to modify (e.g., Hytale:IronSword)")
        .append(new KeyedCodec<>("RecipeId", OptionalCodec.STRING), (c, v) -> c.recipeId = v, c -> c.recipeId).add()
        .documentation("Array of ingredient modifications to apply to this recipe")
        .append(new KeyedCodec<>("Ingredients", new ArrayCodec<>(new OptionalCodec(IngredientModification.CODEC), Optional[]::new)), (c, v) -> c.ingredients = v, c -> c.ingredients
        ).add()
        .build();

    /**
     * The ID of the recipe to modify (e.g., "Hytale:IronSword")
     */
    public Optional<String> recipeId = Optional.empty();

    /**
     * Array of ingredient modifications to apply to this recipe
     */
    public Optional<IngredientModification>[] ingredients =  new Optional[0];

    public RecipeModification() {}

    public RecipeModification(String recipeId, Optional<IngredientModification>[] ingredients) {
        this.recipeId = Optional.of(recipeId);
        this.ingredients = ingredients;
    }
}









