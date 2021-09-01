package net.eonzenx.spool_ge.mixin.entities.animals;

import com.mojang.datafixers.util.Either;
import net.eonzenx.spool_ge.registry_handlers.ItemTagRegistry;
import net.eonzenx.spool_ge.utils.mixin.animals.IBreedableGoalEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.IEdibleGoalEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(OcelotEntity.class)
public abstract class SGEOcelotMixin
{
    protected int edibleItemGoalPriority = 3;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityType<? extends OcelotEntity> entityType, World world, CallbackInfo ci) {
        if (this instanceof IBreedableGoalEntity breedableGoalEntity) {
            breedableGoalEntity.setBreedableItems(Either.left(ItemTagRegistry.OCELOT_BREEDABLE_FOOD));
            breedableGoalEntity.setBreedableItemGoalPriority(edibleItemGoalPriority);

            breedableGoalEntity.completeBreedableGoalSetup();
        }

        if (this instanceof IEdibleGoalEntity edibleGoalEntity) {
            edibleGoalEntity.setEdibleItems(Either.left(ItemTagRegistry.OCELOT_EDIBLE_FOOD));
            edibleGoalEntity.setEdibleItemGoalPriority(edibleItemGoalPriority);

            edibleGoalEntity.completeEdibleGoalSetup();
        }
    }
}
