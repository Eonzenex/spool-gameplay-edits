package net.eonzenx.spool_ge.utils.mixin.animals;

import com.mojang.datafixers.util.Either;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

public interface IEdibleGoalEntity
{
    Either<Tag<Item>, Item> getEdibleItemTags();
    void setEdibleItems(Either<Tag<Item>, Item> newTags);

    int getEdibleItemGoalPriority();
    void setEdibleItemGoalPriority(int newPriority);

    boolean canEatEdible();
    boolean isEdibleItem(ItemStack stack);

    void completeEdibleGoalSetup();
}
