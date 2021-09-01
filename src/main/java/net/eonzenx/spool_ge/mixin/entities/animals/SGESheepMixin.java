package net.eonzenx.spool_ge.mixin.entities.animals;

import com.mojang.datafixers.util.Either;
import net.eonzenx.spool_ge.utils.mixin.animals.IBreedableGoalEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.IEdibleGoalEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(SheepEntity.class)
public abstract class SGESheepMixin
{
    protected int edibleItemGoalPriority = 3;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityType<? extends SheepEntity> entityType, World world, CallbackInfo ci) {
        if (this instanceof IBreedableGoalEntity breedableGoalEntity) {
            breedableGoalEntity.setBreedableItems(Either.right(Items.WHEAT));
            breedableGoalEntity.setBreedableItemGoalPriority(edibleItemGoalPriority);

            breedableGoalEntity.completeBreedableGoalSetup();
        }

        if (this instanceof IEdibleGoalEntity edibleGoalEntity) {
            edibleGoalEntity.setEdibleItems(Either.right(Items.WHEAT));
            edibleGoalEntity.setEdibleItemGoalPriority(edibleItemGoalPriority);

            edibleGoalEntity.completeEdibleGoalSetup();
        }
    }
}
