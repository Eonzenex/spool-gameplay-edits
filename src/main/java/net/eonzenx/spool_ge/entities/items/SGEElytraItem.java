package net.eonzenx.spool_ge.entities.items;

import net.eonzenx.spool_ge.utils.mixin.items.IToolItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ToolMaterial;

public class SGEElytraItem extends ElytraItem implements IToolItem
{
    private final ToolMaterial material;

    public SGEElytraItem(ToolMaterial material, Settings settings) {
        super(settings);
        this.material = material;
    }


    @Override
    public ToolMaterial getMaterial() {
        return this.material;
    }
}
