package net.eonzenx.spool_ge.utils.mixin.animals;

import com.mojang.datafixers.util.Either;
import net.eonzenx.spool_ge.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class PickupItemGoal extends Goal {
    public final AnimalEntity animal;
    public final Either<Tag<Item>, Item> edibleItems;
    public final Predicate<ItemEntity> PICKABLE_DROP_FILTER;
    public final int edibleType;


    private boolean onEatTimeout() {
        if (animal instanceof IHappinessEntity happinessEntity) {
            return happinessEntity.onEatTimeout();
        }

        return false;
    }

    public PickupItemGoal(AnimalEntity animal, Either<Tag<Item>, Item> edibleItems, int edibleType) {
        this.setControls(EnumSet.of(Control.MOVE));
        this.animal = animal;
        this.edibleItems = edibleItems;
        this.edibleType = edibleType;

        PICKABLE_DROP_FILTER = (item) -> {
            if (onEatTimeout()) return false;

            var always = !item.cannotPickup() && item.isAlive();

            // Don't path baby animals to breedable items
            if (edibleType == 0) {
                if (animal.isBaby()) return false;
            }

            // Only path to edible goal if happiness does not exceed fraction
            if (edibleType == 1) {
                if (Config.Animals.Happiness.calcHappinessRatio(animal) > 1) return false;
            }

            var result = new AtomicBoolean(false);
            edibleItems.ifLeft(itemTags -> result.set(result.get() || (always && item.getStack().isIn(itemTags))));
            edibleItems.ifRight(edibleItem -> result.set(result.get() || (always && item.getStack().isOf(edibleItem))));

            return result.get();
        };
    }

    public boolean canStart() {
        if (animal.getTarget() != null || animal.getAttacker() != null) return false;
        if (animal.getRandom().nextInt(10) != 0) return false;
        if (onEatTimeout()) return false;

        List<ItemEntity> list = animal.world.getEntitiesByClass(ItemEntity.class, animal.getBoundingBox().expand(8.0D, 8.0D, 8.0D), PICKABLE_DROP_FILTER);
        return !list.isEmpty() && animal.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
    }

    public void tick() {
        if (onEatTimeout()) return;

        List<ItemEntity> list = animal.world.getEntitiesByClass(ItemEntity.class, animal.getBoundingBox().expand(8.0D, 8.0D, 8.0D), PICKABLE_DROP_FILTER);
        ItemStack itemStack = animal.getEquippedStack(EquipmentSlot.MAINHAND);
        if (itemStack.isEmpty() && !list.isEmpty()) {
            animal.getNavigation().startMovingTo((Entity) list.get(0), 1.2000000476837158D);
        }
    }

    public void start() {
        List<ItemEntity> list = animal.world.getEntitiesByClass(ItemEntity.class, animal.getBoundingBox().expand(8.0D, 8.0D, 8.0D), PICKABLE_DROP_FILTER);
        if (!list.isEmpty()) {
            animal.getNavigation().startMovingTo((Entity) list.get(0), 1.2000000476837158D);
        }
    }
}
