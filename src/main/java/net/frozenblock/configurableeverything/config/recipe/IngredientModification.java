package net.frozenblock.configurableeverything.config.recipe;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import net.frozenblock.configurableeverything.CEPlugin;
import net.frozenblock.configurableeverything.codec.OptionalCodec;

import java.util.Optional;

/**
 * Represents a modification to an ingredient in a crafting recipe.
 * Can be used to add, remove, or modify the quantity of an ingredient.
 */
public class IngredientModification {
    public static final BuilderCodec<IngredientModification> CODEC = BuilderCodec.builder(IngredientModification.class, IngredientModification::new)
        .documentation("Specific item ID to match (e.g., Hytale:IronIngot)")
        .append(new KeyedCodec<>("ItemId", OptionalCodec.STRING), (c, v) -> c.itemId = CEPlugin.opt(v), c -> c.itemId).add()
        .documentation("Resource type ID for generic matching (e.g., Hytale:Metal)")
        .append(new KeyedCodec<>("ResourceTypeId", OptionalCodec.STRING), (c, v) -> c.resourceTypeId = CEPlugin.opt(v), c -> c.resourceTypeId).add()
        .documentation("Item tag for tag-based matching")
        .append(new KeyedCodec<>("ItemTag", OptionalCodec.STRING), (c, v) -> c.itemTag = CEPlugin.opt(v), c -> c.itemTag).add()
        .documentation("New quantity for this ingredient (only when Remove=false)")
        .append(new KeyedCodec<>("Quantity", OptionalCodec.INTEGER), (c, v) -> c.quantity = CEPlugin.opt(v), c -> c.quantity).add()
        .documentation("If true, removes the ingredient; if false, adds or modifies it")
        .append(new KeyedCodec<>("Remove", Codec.NULLABLE_BOOLEAN), (c, v) -> c.remove = v, c -> c.remove).add()
        .build();

    /**
     * The item ID to match (if specified, will identify the ingredient to modify)
     */
    public Optional<String> itemId = Optional.empty();

    /**
     * Alternative: resource type ID for generic matching
     */
    public Optional<String> resourceTypeId = Optional.empty();

    /**
     * Alternative: item tag for tag-based matching
     */
    public Optional<String> itemTag = Optional.empty();

    /**
     * The new quantity for this ingredient. Leave empty to keep the original quantity.
     * Only used if remove=false.
     */
    public Optional<Integer> quantity = Optional.empty();

    /**
     * If true, this ingredient will be removed from the recipe.
     * If false, this ingredient will be added or modified.
     */
    public boolean remove = false;

    public IngredientModification() {}

    public IngredientModification(String itemId, Optional<Integer> quantity, boolean remove) {
        this.itemId = Optional.ofNullable(itemId);
        this.quantity = quantity;
        this.remove = remove;
    }
}


