package net.eonzenx.spool_ge.mixin;

import net.eonzenx.spool_ge.utils.mixin.items.IToolItem;
import net.minecraft.item.ToolItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ToolItem.class)
public abstract class SGEToolItem implements IToolItem
{

}
