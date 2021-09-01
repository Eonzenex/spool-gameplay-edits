package net.eonzenx.spool_ge.utils.mixin.animals;

import net.minecraft.item.ItemStack;

public interface IHappinessEntity
{
    boolean canEat();

    float getHappiness();
    void setHappiness(float newHappiness);
    float addHappiness(float amount);

    int getEatTimeout();
    void setEatTimeout(int newEatTimer);
    boolean onEatTimeout();

    int getBreedingAge();
    boolean isBaby();

    void eat(ItemStack stack);
    void postEat(ItemStack stack);
}
