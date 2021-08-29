package net.eonzenx.spool_ge.mixin;

import com.mojang.authlib.GameProfile;
import net.eonzenx.spool_ge.utils.mixin.travel.ITickMovement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayerEntity.class)
public abstract class SGEClientPlayerEntityMixin extends PlayerEntity
{
    public SGEClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }


    @Shadow public int ticksSinceSprintingChanged;
    @Shadow protected int ticksLeftToDoubleTapSprint;
    @Shadow public Input input;
    @Shadow private boolean inSneakingPose;
    @Shadow @Final protected MinecraftClient client;
    @Shadow private int ticksToNextAutojump;
    @Shadow private boolean falling;
    @Shadow private int underwaterVisibilityTicks;
    @Shadow @Final public ClientPlayNetworkHandler networkHandler;
    @Shadow private int field_3938;
    @Shadow private float mountJumpStrength;

    @Shadow private void updateNausea() {}
    @Shadow private boolean isWalking() { return true; }
    @Shadow public abstract boolean shouldSlowDown();
    @Shadow private void pushOutOfBlocks(double x, double z) {}
    @Shadow protected abstract boolean isCamera();
    @Shadow public abstract boolean hasJumpingMount();
    @Shadow public abstract float getMountJumpStrength();
    @Shadow protected abstract void startRidingJump();


    protected void slowIfUsingItem() {
        if (this.isUsingItem() && !this.hasVehicle()) {
            var input = this.input;
            input.movementSideways *= 0.2F;
            input = this.input;
            input.movementForward *= 0.2F;
            this.ticksLeftToDoubleTapSprint = 0;
        }
    }

    protected boolean isTryingToAutoJump() {
        var isTryingToAutoJump = false;
        if (this.ticksToNextAutojump > 0) {
            --this.ticksToNextAutojump;
            isTryingToAutoJump = true;
            this.input.jumping = true;
        }

        return isTryingToAutoJump;
    }

    protected void tryNudgePlayer() {
        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double) this.getWidth() * 0.35D, this.getZ() + (double) this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() - (double) this.getWidth() * 0.35D, this.getZ() - (double) this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() + (double) this.getWidth() * 0.35D, this.getZ() - (double) this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() + (double) this.getWidth() * 0.35D, this.getZ() + (double) this.getWidth() * 0.35D);
        }
    }

    protected boolean canSprint() {
        return (float) this.getHungerManager().getFoodLevel() > 6.0F || this.getAbilities().allowFlying;
    }

    protected void trySprintWeird(boolean isSneaking, boolean isWalking, boolean canSprint) {
        // Tf if this not walking && is walking bullshit?
        if ((this.onGround || this.isSubmergedInWater()) && !isSneaking && !isWalking && this.isWalking() && !this.isSprinting() && canSprint && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS)) {
            if (this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.keySprint.isPressed()) {
                this.ticksLeftToDoubleTapSprint = 7;
            } else {
                this.setSprinting(true);
            }
        }
    }

    protected void trySprint(boolean canSprint) {
        if (!this.isSprinting() && (!this.isTouchingWater() || this.isSubmergedInWater()) && this.isWalking() && canSprint && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && this.client.options.keySprint.isPressed()) {
            this.setSprinting(true);
        }
    }

    protected boolean tryBlockSprint(boolean canSprint) {
        boolean blockSprint;
        if (this.isSprinting()) {
            blockSprint = !this.input.hasForwardMovement() || !canSprint;
            boolean bl7 = blockSprint || this.horizontalCollision || this.isTouchingWater() && !this.isSubmergedInWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.sneaking && blockSprint || !this.isTouchingWater()) {
                    this.setSprinting(false);
                }
            } else if (bl7) {
                this.setSprinting(false);
            }
        }

        return false;
    }

    protected boolean tryBlockSprintWeird(boolean isJumping, boolean isTryingToJump) {
        var blockSprint = false;
        if (this.getAbilities().allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    blockSprint = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!isJumping && this.input.jumping && !isTryingToJump) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    blockSprint = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }

        return blockSprint;
    }

    protected void tryGliding(boolean blockSprint, boolean isJumping) {
        if (this.input.jumping && !blockSprint && !isJumping && !this.getAbilities().flying && !this.hasVehicle() && !this.isClimbing()) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.getItem() instanceof ElytraItem && ElytraItem.isUsable(itemStack) && this.checkFallFlying()) {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        this.falling = this.isFallFlying();
    }

    protected void trySink() {
        if (this.isTouchingWater() && this.input.sneaking && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
    }

    protected int jUnderwaterVisibilityTick() {
        var j = 0;
        if (this.isSubmergedIn(FluidTags.WATER)) {
            j = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + j, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }

        return j;
    }

    protected int updateSpectatorVelocity(int j) {
        if (this.getAbilities().flying && this.isCamera()) {
            j = 0;
            if (this.input.sneaking) {
                --j;
            }

            if (this.input.jumping) {
                ++j;
            }

            if (j != 0) {
                this.setVelocity(this.getVelocity().add(0.0D, (double)((float)j * this.getAbilities().getFlySpeed() * 3.0F), 0.0D));
            }
        }

        return j;
    }

    protected void tryJumpMount(boolean isJumping) {
        if (this.hasJumpingMount()) {
            JumpingMount jumpingMount = (JumpingMount) this.getVehicle();
            if (this.field_3938 < 0) {
                ++this.field_3938;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0F;
                }
            }

            if (isJumping && !this.input.jumping) {
                this.field_3938 = -10;
                jumpingMount.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0F));
                this.startRidingJump();
            } else if (!isJumping && this.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0F;
            } else if (isJumping) {
                ++this.field_3938;
                if (this.field_3938 < 10) {
                    this.mountJumpStrength = (float)this.field_3938 * 0.1F;
                } else {
                    this.mountJumpStrength = 0.8F + 2.0F / (float)(this.field_3938 - 9) * 0.1F;
                }
            }
        } else {
            this.mountJumpStrength = 0.0F;
        }
    }

    protected void tryStopFlying() {
        if (this.onGround && this.getAbilities().flying && !this.client.interactionManager.isFlyingLocked()) {
            this.getAbilities().flying = false;
            this.sendAbilitiesUpdate();
        }
    }


    @Override
    public void tickMovement() {
        ++this.ticksSinceSprintingChanged;
        if (this.ticksLeftToDoubleTapSprint > 0) {
            --this.ticksLeftToDoubleTapSprint;
        }

        this.updateNausea();

        var isJumping = this.input.jumping;
        var isSneaking = this.input.sneaking;
        var isWalking = this.isWalking();

        this.inSneakingPose = !this.getAbilities().flying && !this.isSwimming() && this.wouldPoseNotCollide(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.wouldPoseNotCollide(EntityPose.STANDING));
        this.input.tick(this.shouldSlowDown());
        this.client.getTutorialManager().onMovement(this.input);

        slowIfUsingItem();

        var isTryingToJump = isTryingToAutoJump();

        tryNudgePlayer();

        if (isSneaking) {
            this.ticksLeftToDoubleTapSprint = 0;
        }

        var canSprint = canSprint();
        trySprintWeird(isSneaking, isWalking, canSprint);
        trySprint(canSprint);

        var blockSprint = tryBlockSprint(canSprint);
        blockSprint = tryBlockSprintWeird(isJumping, isTryingToJump);

        tryGliding(blockSprint, isJumping);

        trySink();

        var j = jUnderwaterVisibilityTick();
        j = updateSpectatorVelocity(j);

        tryJumpMount(isJumping);
        super.tickMovement();
        tryStopFlying();
    }
}
