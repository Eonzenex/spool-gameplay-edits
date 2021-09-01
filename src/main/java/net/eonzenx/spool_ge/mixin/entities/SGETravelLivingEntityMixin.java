package net.eonzenx.spool_ge.mixin.entities;

import net.eonzenx.spool_ge.entities.items.SGEElytraItem;
import net.eonzenx.spool_ge.config.Config;
import net.eonzenx.spool_ge.utils.mixin.travel.ITravel;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SGETravelLivingEntityMixin extends Entity implements ITravel
{
    public SGETravelLivingEntityMixin(EntityType<?> type, World world) { super(type, world); }

    @Shadow protected int roll;

    @Shadow public abstract boolean canMoveVoluntarily();
    @Shadow public abstract boolean canWalkOnFluid(Fluid fluid);
    @Shadow protected abstract boolean shouldSwimInFluids();
    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);
    @Shadow protected abstract float getBaseMovementSpeedMultiplier();
    @Shadow public abstract float getMovementSpeed();
    @Shadow public abstract boolean isClimbing();
    @Shadow public abstract Vec3d method_26317(double d, boolean bl, Vec3d vec3d);
    @Shadow public abstract boolean isFallFlying();
    @Shadow protected abstract SoundEvent getFallSound(int distance);
    @Shadow public abstract Vec3d method_26318(Vec3d vec3d, float f);
    @Shadow public abstract boolean hasNoDrag();
    @Shadow @Nullable public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
    @Shadow public abstract void updateLimbs(LivingEntity entity, boolean flutter);


    public void travelSwimming(Vec3d movementInput, double fallSpeed, boolean isFalling) {
        var e = this.getY();
        var j = this.isSprinting() ? 0.9F : this.getBaseMovementSpeedMultiplier();
        var g = 0.02F;

        var h = (float) EnchantmentHelper.getDepthStrider((LivingEntity) (Object) this);
        if (h > 3.0F) {
            h = 3.0F;
        }
        if (!this.onGround) {
            h *= 0.5F;
        }

        if (h > 0.0F) {
            j += (0.54600006F - j) * h / 3.0F;
            g += (this.getMovementSpeed() - g) * h / 3.0F;
        }

        if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            j = 0.96F;
        }

        this.updateVelocity(g, movementInput);
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d vec3d = this.getVelocity();
        if (this.horizontalCollision && this.isClimbing()) {
            vec3d = new Vec3d(vec3d.x, 0.2D, vec3d.z);
        }

        this.setVelocity(vec3d.multiply(j, 0.800000011920929D, j));

        Vec3d vec3d2 = this.method_26317(fallSpeed, isFalling, this.getVelocity());
        this.setVelocity(vec3d2);

        if (this.horizontalCollision && this.doesNotCollide(vec3d2.x, vec3d2.y + 0.6000000238418579D - this.getY() + e, vec3d2.z)) {
            this.setVelocity(vec3d2.x, 0.30000001192092896D, vec3d2.z);
        }
    }


    public void travelLavaSwimming(Vec3d movementInput, double fallSpeed, boolean isFalling) {
        var e = this.getY();
        this.updateVelocity(0.02F, movementInput);
        this.move(MovementType.SELF, this.getVelocity());

        Vec3d vec3d4;
        if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
            this.setVelocity(this.getVelocity().multiply(0.5D, 0.800000011920929D, 0.5D));
            vec3d4 = this.method_26317(fallSpeed, isFalling, this.getVelocity());
            this.setVelocity(vec3d4);
        } else {
            this.setVelocity(this.getVelocity().multiply(0.5D));
        }

        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -fallSpeed / 4.0D, 0.0D));
        }

        vec3d4 = this.getVelocity();
        if (this.horizontalCollision && this.doesNotCollide(vec3d4.x, vec3d4.y + 0.6000000238418579D - this.getY() + e, vec3d4.z)) {
            this.setVelocity(vec3d4.x, 0.30000001192092896D, vec3d4.z);
        }
    }


    private float calcGlidePitchMultiplier(Item item) {
        if (item instanceof SGEElytraItem sgeElytraItem) {
            var material = sgeElytraItem.getMaterial();

            if (material == ToolMaterials.WOOD) {
                return Config.Wings.Leaf.PITCH_MULTIPLIER;
            } else if (material == ToolMaterials.STONE) {
                return Config.Wings.Feather.PITCH_MULTIPLIER;
            } else if (material == ToolMaterials.IRON) {
                return Config.Wings.Hide.PITCH_MULTIPLIER;
            } else if (material == ToolMaterials.DIAMOND) {
                return Config.Wings.Elytra.PITCH_MULTIPLIER;
            }
        }

        return 1f;
    }

    public void travelGliding(Vec3d movementInput, double fallSpeed) {
        var velocity = this.getVelocity();

        if (velocity.y > -0.5D) {
            this.fallDistance = 1.0F;
        }

        var rotation = this.getRotationVector();
        var j = this.getPitch() * 0.017453292F;
        var xzRotationLength = Math.sqrt(rotation.x * rotation.x + rotation.z * rotation.z);
        var xzVelocityLength = velocity.horizontalLength();
        var rotationLength = rotation.length();

        var n = MathHelper.cos(j);
        n = (float) ((double) n * (double) n * Math.min(1.0D, rotationLength / 0.4D));

        var item = this.getEquippedStack(EquipmentSlot.CHEST).getItem();
        n *= calcGlidePitchMultiplier(item);

        velocity = this.getVelocity().add(0.0D, fallSpeed * (-1.0D + (double) n * 0.75D), 0.0D);
        double q;
        if (velocity.y < 0.0D && xzRotationLength > 0.0D) {
            q = velocity.y * -0.1D * (double) n;
            velocity = velocity.add(rotation.x * q / xzRotationLength, q, rotation.z * q / xzRotationLength);
        }

        if (j < 0.0F && xzRotationLength > 0.0D) {
            q = xzVelocityLength * (double) (-MathHelper.sin(j)) * 0.04D;
            velocity = velocity.add(-rotation.x * q / xzRotationLength, q * 3.2D, -rotation.z * q / xzRotationLength);
        }

        if (xzRotationLength > 0.0D) {
            velocity = velocity.add((rotation.x / xzRotationLength * xzVelocityLength - velocity.x) * 0.1D, 0.0D, (rotation.z / xzRotationLength * xzVelocityLength - velocity.z) * 0.1D);
        }

        this.setVelocity(velocity.multiply(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D));
        this.move(MovementType.SELF, this.getVelocity());

        if (this.horizontalCollision && !this.world.isClient) {
            q = this.getVelocity().horizontalLength();
            double r = xzVelocityLength - q;
            float s = (float) (r * 10.0D - 3.0D);
            if (s > 0.0F) {
                this.playSound(this.getFallSound((int) s), 1.0F, 1.0F);
                this.damage(DamageSource.FLY_INTO_WALL, s);
            }
        }

        if (this.onGround && !this.world.isClient) {
            this.setFlag(7, false);
        }
    }


    public void travelOther(Vec3d movementInput, double fallSpeed) {
        BlockPos blockPos = this.getVelocityAffectingPos();
        float standingBlockSlipperiness = this.world.getBlockState(blockPos).getBlock().getSlipperiness();
        var blockSlipperiness = this.onGround ? standingBlockSlipperiness * 0.91F : 0.91F;

        Vec3d vec3d7 = this.method_26318(movementInput, standingBlockSlipperiness);
        double v = vec3d7.y;
        if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
            var levitationEffect = this.getStatusEffect(StatusEffects.LEVITATION);
            var levitationAmplifier = 1;
            if (levitationEffect != null) {
                levitationAmplifier = levitationEffect.getAmplifier() + 1;
            }

            v += (0.05D * (double) levitationAmplifier - vec3d7.y) * 0.2D;
            this.fallDistance = 0.0F;
        } else if (this.world.isClient && !this.world.isChunkLoaded(blockPos)) {
            if (this.getY() > (double) this.world.getBottomY()) {
                v = -0.1D;
            } else {
                v = 0.0D;
            }
        } else if (!this.hasNoGravity()) {
            v -= fallSpeed;
        }

        if (this.hasNoDrag()) {
            this.setVelocity(vec3d7.x, v, vec3d7.z);
        } else {
            this.setVelocity(vec3d7.x * (double) blockSlipperiness, v * 0.9800000190734863D, vec3d7.z * (double) blockSlipperiness);
        }
    }


    /**
     * @author EonZeNx
     * @reason Can't change the glide code halfway through the function without overriding
     * the entire function. All original travel code is maintained but sectioned into
     * functions to allow other mods to hook into this mixin without overriding this mod.
     */
    @Override
    public void travel(Vec3d movementInput) {
        if (this.canMoveVoluntarily() || this.isLogicalSideForUpdatingMovement()) {
            var fluidState = this.world.getFluidState(this.getBlockPos());

            var fallSpeed = 0.08D;
            var isFalling = this.getVelocity().y <= 0.0D;
            if (isFalling && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                fallSpeed = 0.01D;
                this.fallDistance = 0.0F;
            }

            if (this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState.getFluid())) {
                travelSwimming(movementInput, fallSpeed, isFalling);
            } else if (this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState.getFluid())) {
                travelLavaSwimming(movementInput, fallSpeed, isFalling);
            } else if (this.isFallFlying()) {
                travelGliding(movementInput, fallSpeed);
            } else {
                travelOther(movementInput, fallSpeed);
            }
        }

        this.updateLimbs((LivingEntity) (Object) this, this instanceof Flutterer);
    }


    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private static void getPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (stack.getItem() instanceof ElytraItem) {
            cir.setReturnValue(EquipmentSlot.CHEST);
        }
    }

    /**
     * @author EonZeNx
     * @reason Either HEAD or TAIL inject results in weird behaviour, have to override the function.
     * 
     */
    @Inject(method = "tickFallFlying", at = @At("INVOKE"), cancellable = true)
    private void tickFallFlying(CallbackInfo ci) {
        var isFallFlying = this.getFlag(7);
        if (isFallFlying && !this.onGround && !this.hasVehicle() && !this.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.getItem() instanceof ElytraItem && ElytraItem.isUsable(itemStack)) {
                isFallFlying = true;
                int i = this.roll + 1;
                if (!this.world.isClient && i % 10 == 0) {
                    int j = i / 10;
                    if (j % 2 == 0) {
                        itemStack.damage(1, (LivingEntity) (Object) this, (player) -> {
                            player.sendEquipmentBreakStatus(EquipmentSlot.CHEST);
                        });
                    }

                    this.emitGameEvent(GameEvent.ELYTRA_FREE_FALL);
                }
            } else {
                isFallFlying = false;
            }
        } else {
            isFallFlying = false;
        }

        if (!this.world.isClient) {
            this.setFlag(7, isFallFlying);
        }

        ci.cancel();
    }
}
