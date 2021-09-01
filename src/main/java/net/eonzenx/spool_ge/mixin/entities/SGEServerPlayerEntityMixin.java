package net.eonzenx.spool_ge.mixin.entities;

import net.eonzenx.spool_ge.config.Config;
import net.eonzenx.spool_ge.utils.mixin.server.IBedRespawn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class SGEServerPlayerEntityMixin extends LivingEntity implements IBedRespawn
{
    protected SGEServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    private int bedRespawnCount;


    @Override
    public int getBedRespawn() { return bedRespawnCount; }

    @Override
    public void setBedRespawn(int newBedRespawn) { bedRespawnCount = newBedRespawn; }

    @Override
    public int incrementBedRespawn() {
        bedRespawnCount++;

        if (bedRespawnCount >= Config.Bed.MAX_BED_RESPAWNS) {
            ((ServerPlayerEntity) (Object) this)
                    .setSpawnPoint(this.getServerWorld().getRegistryKey(), null, 0, false, false);
        }

        return bedRespawnCount;
    }


    @Inject(method = "wakeUp", at = @At("HEAD"))
    public void wakeUp(boolean bl, boolean updateSleepingPlayers, CallbackInfo ci) {
        if (this.isSleeping()) {
            bedRespawnCount = 0;
        }
    }
}
