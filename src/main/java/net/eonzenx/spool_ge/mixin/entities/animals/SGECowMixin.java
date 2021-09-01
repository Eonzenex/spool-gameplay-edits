package net.eonzenx.spool_ge.mixin.entities.animals;

import com.mojang.datafixers.util.Either;
import net.eonzenx.spool_ge.registry_handlers.ItemTagRegistry;
import net.eonzenx.spool_ge.utils.mixin.animals.IBreedableGoalEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.IEdibleGoalEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CowEntity.class)
public abstract class SGECowMixin
{
    protected int edibleItemGoalPriority = 3;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityType<? extends CowEntity> entityType, World world, CallbackInfo ci) {
        if (this instanceof IBreedableGoalEntity breedableGoalEntity) {
            breedableGoalEntity.setBreedableItems(Either.right(Items.WHEAT));
            breedableGoalEntity.setBreedableItemGoalPriority(edibleItemGoalPriority);

            breedableGoalEntity.completeBreedableGoalSetup();
        }

        if (this instanceof IEdibleGoalEntity edibleGoalEntity) {
            edibleGoalEntity.setEdibleItems(Either.left(ItemTagRegistry.COW_EDIBLE_FOOD));
            edibleGoalEntity.setEdibleItemGoalPriority(edibleItemGoalPriority);

            edibleGoalEntity.completeEdibleGoalSetup();
        }
    }
}
