package net.eonzenx.spool_ge.mixin.entities.animals;

import com.mojang.datafixers.util.Either;
import net.eonzenx.spool_ge.utils.mixin.animals.IBreedableGoalEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.IEdibleGoalEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoxEntity.class)
public abstract class SGEFoxMixin
{
    protected int edibleItemGoalPriority = 4;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityType<? extends FoxEntity> entityType, World world, CallbackInfo ci) {
        if (this instanceof IBreedableGoalEntity breedableGoalEntity) {
            breedableGoalEntity.setBreedableItems(Either.right(Items.SWEET_BERRIES));
            breedableGoalEntity.setBreedableItemGoalPriority(edibleItemGoalPriority);

            breedableGoalEntity.completeBreedableGoalSetup();
        }

        if (this instanceof IEdibleGoalEntity edibleGoalEntity) {
            edibleGoalEntity.setEdibleItems(Either.left(ItemTags.FOX_FOOD));
            edibleGoalEntity.setEdibleItemGoalPriority(edibleItemGoalPriority);

            edibleGoalEntity.completeEdibleGoalSetup();
        }
    }
}
