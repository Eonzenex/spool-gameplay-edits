package net.eonzenx.spool_ge.registry_handlers;

import net.eonzenx.spool_ge.SpoolGameplayEdits;
import net.eonzenx.spool_ge.entities.items.SGEElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class ItemRegistry
{
    public static final SGEElytraItem LEAF_WINGS;
    public static final SGEElytraItem FEATHER_WINGS;
    public static final SGEElytraItem HIDE_WINGS;
    public static final SGEElytraItem ELYTRA_WINGS;

    public static final Item LEAF_WING;
    public static final Item FEATHER_WING;
    public static final Item HIDE_WING;


    public static void init() {
        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("leaf_wings"), LEAF_WINGS);
        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("feather_wings"), FEATHER_WINGS);
        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("hide_wings"), HIDE_WINGS);
        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("elytra_wings"), ELYTRA_WINGS);

        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("leaf_wing"), LEAF_WING);
        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("feather_wing"), FEATHER_WING);
        Registry.register(Registry.ITEM, SpoolGameplayEdits.newId("hide_wing"), HIDE_WING);
    }

    static {
        LEAF_WINGS = new SGEElytraItem(ToolMaterials.WOOD, new Item.Settings().maxDamage(102).group(ItemGroup.TRANSPORTATION).rarity(Rarity.COMMON));
        FEATHER_WINGS = new SGEElytraItem(ToolMaterials.STONE, new Item.Settings().maxDamage(215).group(ItemGroup.TRANSPORTATION).rarity(Rarity.COMMON));
        HIDE_WINGS = new SGEElytraItem(ToolMaterials.IRON, new Item.Settings().maxDamage(322).group(ItemGroup.TRANSPORTATION).rarity(Rarity.UNCOMMON));
        ELYTRA_WINGS = new SGEElytraItem(ToolMaterials.DIAMOND, new Item.Settings().maxDamage(432).group(ItemGroup.TRANSPORTATION).rarity(Rarity.UNCOMMON));

        LEAF_WING = new Item(new Item.Settings().group(ItemGroup.TRANSPORTATION).rarity(Rarity.COMMON).maxCount(2));
        FEATHER_WING = new Item(new Item.Settings().group(ItemGroup.TRANSPORTATION).rarity(Rarity.COMMON).maxCount(2));
        HIDE_WING = new Item(new Item.Settings().group(ItemGroup.TRANSPORTATION).rarity(Rarity.COMMON).maxCount(2));
    }
}
