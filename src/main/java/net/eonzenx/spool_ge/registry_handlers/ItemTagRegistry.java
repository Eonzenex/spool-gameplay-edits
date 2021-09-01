package net.eonzenx.spool_ge.registry_handlers;

import net.eonzenx.spool_ge.SpoolGameplayEdits;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

public class ItemTagRegistry
{
    public static final Tag<Item> CHICKEN_BREEDABLE_FOOD;
    public static final Tag<Item> COW_EDIBLE_FOOD;
    public static final Tag<Item> HORSE_EDIBLE_FOOD;
    public static final Tag<Item> OCELOT_BREEDABLE_FOOD;
    public static final Tag<Item> OCELOT_EDIBLE_FOOD;
    public static final Tag<Item> PIG_BREEDABLE_FOOD;
    public static final Tag<Item> PIG_EDIBLE_FOOD;

    static {
        CHICKEN_BREEDABLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("chicken_breedable_food"));
        COW_EDIBLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("cow_edible_food"));
        HORSE_EDIBLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("horse_edible_food"));
        OCELOT_BREEDABLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("ocelot_breedable_food"));
        OCELOT_EDIBLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("ocelot_edible_food"));
        PIG_BREEDABLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("pig_breedable_food"));
        PIG_EDIBLE_FOOD = TagRegistry.item(SpoolGameplayEdits.newId("pig_edible_food"));
    }
}
