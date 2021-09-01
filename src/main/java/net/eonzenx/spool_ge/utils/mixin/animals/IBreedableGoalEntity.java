package net.eonzenx.spool_ge.utils.mixin.animals;

import com.mojang.datafixers.util.Either;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

public interface IBreedableGoalEntity
{
    Either<Tag<Item>, Item> getBreedableItemTags();
    void setBreedableItems(Either<Tag<Item>, Item> newItems);

    int getBreedableItemGoalPriority();
    void setBreedableItemGoalPriority(int newPriority);

    boolean canEatBreedable();
    boolean isBreedableItem(ItemStack stack);

    void completeBreedableGoalSetup();
}
