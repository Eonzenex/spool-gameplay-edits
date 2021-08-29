package net.eonzenx.spool_ge.utils.mixin.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;

public interface IToolItem
{
    ToolMaterial getMaterial();
    int getEnchantability();
    boolean canRepair(ItemStack stack, ItemStack ingredient);
}
