package net.eonzenx.spool_ge.mixin;

import net.eonzenx.spool_ge.utils.mixin.travel.ICheckGliding;
import net.eonzenx.spool_ge.utils.mixin.travel.ITravel;
import net.eonzenx.spool_ge.utils.mixin.travel.ITravelFlying;
import net.eonzenx.spool_ge.utils.mixin.travel.ITravelSwimming;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class SGEPlayerEntityMixin extends LivingEntity implements ITravel, ITravelFlying, ITravelSwimming, ICheckGliding
{
    protected SGEPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @Shadow @Final private PlayerAbilities abilities;

    @Shadow public abstract void increaseTravelMotionStats(double dx, double dy, double dz);
    @Shadow public abstract void startFallFlying();


    @Override
    public void travelFlying(Vec3d movementInput) {
        var i = this.getVelocity().y;
        float j = this.flyingSpeed;
        this.flyingSpeed = this.abilities.getFlySpeed() * (float) (this.isSprinting() ? 2 : 1);
        super.travel(movementInput);
        Vec3d vec3d2 = this.getVelocity();
        this.setVelocity(vec3d2.x, i * 0.6D, vec3d2.z);
        this.flyingSpeed = j;
        this.fallDistance = 0.0F;
        this.setFlag(7, false);
    }

    @Override
    public void travelSwimming(Vec3d movementInput) {
        var i = this.getRotationVector().y;
        double h = i < -0.2D ? 0.085D : 0.06D;
        if (i <= 0.0D || this.jumping || !this.world.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0D - 0.1D, this.getZ())).getFluidState().isEmpty()) {
            Vec3d vec3d = this.getVelocity();
            this.setVelocity(vec3d.add(0.0D, (i - vec3d.y) * h, 0.0D));
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        var d = this.getX();
        var e = this.getY();
        var f = this.getZ();

        if (this.isSwimming() && !this.hasVehicle()) {
            travelSwimming(movementInput);
        }

        if (this.abilities.flying && !this.hasVehicle()) {
            travelFlying(movementInput);
        } else {
            super.travel(movementInput);
        }

        this.increaseTravelMotionStats(this.getX() - d, this.getY() - e, this.getZ() - f);
    }

    @Override
    public boolean checkFallFlying() {
        if (!this.onGround && !this.isFallFlying() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);

            var item = itemStack.getItem();
            var usable = ElytraItem.isUsable(itemStack);

            if (item instanceof ElytraItem && usable) {
                this.startFallFlying();
                return true;
            }
        }

        return false;
    }
}
