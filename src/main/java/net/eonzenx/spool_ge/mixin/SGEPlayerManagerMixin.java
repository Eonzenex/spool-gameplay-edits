package net.eonzenx.spool_ge.mixin;

import net.eonzenx.spool_ge.config.Config;
import net.eonzenx.spool_ge.utils.mixin.server.IBedRespawn;
import net.eonzenx.spool_ge.utils.mixin.server.IRespawnPlayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(PlayerManager.class)
public abstract class SGEPlayerManagerMixin implements IRespawnPlayer
{
    @Shadow @Final private List<ServerPlayerEntity> players;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private Map<UUID, ServerPlayerEntity> playerMap;

    @Shadow public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);
    @Shadow public abstract void sendCommandTree(ServerPlayerEntity player);


    private void removePlayerFromServerList(ServerPlayerEntity player) {
        this.players.remove(player);
        player.getServerWorld().removePlayer(player, Entity.RemovalReason.DISCARDED);
    }

    private BlockPos getRespawnPoint(ServerPlayerEntity player) {
        var spawnPos = player.getSpawnPointPosition();

        if (player.getServerWorld().getBlockState(spawnPos).isOf(Blocks.RESPAWN_ANCHOR) && !Config.Bed.AFFECT_RESPAWN_ANCHOR) {
            return spawnPos;
        }

        if (player instanceof IBedRespawn bedRespawn) {
            if (bedRespawn.getBedRespawn() >= Config.Bed.MAX_BED_RESPAWNS) {
                return player.getServerWorld().getSpawnPos();
            }

            if (spawnPos != player.getServerWorld().getSpawnPos()) {
                bedRespawn.incrementBedRespawn();
            }
        }

        return spawnPos;
    }

    private float getRespawnAngle(ServerPlayerEntity player) {
        return player.getSpawnAngle();
    }

    private boolean getIsSpawnPointSet(ServerPlayerEntity player) {
        return player.isSpawnPointSet();
    }

    private ServerWorld getServerWorld(ServerPlayerEntity player) {
        return this.server.getWorld(player.getSpawnPointDimension());
    }

    private Optional<Vec3d> getValidRespawnPosition(ServerWorld serverWorld, BlockPos respawnBlockPos, float respawnAngle, boolean isSpawnPointSet, boolean alive) {
        if (serverWorld != null && respawnBlockPos != null) {
            return PlayerEntity.findRespawnPosition(serverWorld, respawnBlockPos, respawnAngle, isSpawnPointSet, alive);
        } else {
            return Optional.empty();
        }
    }

    private ServerWorld getValidServerWorld(ServerWorld serverWorld, Optional<Vec3d> validRespawnPosition) {
        return serverWorld != null && validRespawnPosition.isPresent() ? serverWorld : this.server.getOverworld();
    }

    private ServerPlayerEntity copyScoreboardTags(ServerPlayerEntity speNew, ServerPlayerEntity speOld) {
        for (String string : speOld.getScoreboardTags()) {
            speNew.addScoreboardTag(string);
        }

        if (speNew instanceof IBedRespawn bedNew && speOld instanceof IBedRespawn bedOld) {
            bedNew.setBedRespawn(bedOld.getBedRespawn());
        }

        return speNew;
    }

    private ServerPlayerEntity setupNewServerPlayerEntity(ServerPlayerEntity player, ServerWorld validServerWorld, boolean alive) {
        var serverPlayerEntity = new ServerPlayerEntity(this.server, validServerWorld, player.getGameProfile());
        serverPlayerEntity.networkHandler = player.networkHandler;
        serverPlayerEntity.copyFrom(player, alive);
        serverPlayerEntity.setId(player.getId());
        serverPlayerEntity.setMainArm(player.getMainArm());

        return copyScoreboardTags(serverPlayerEntity, player);
    }

    private Optional<Vec3d> bedRespawnCheck(ServerPlayerEntity player, Optional<Vec3d> validRespawnPosition, BlockPos respawnBlockPos) {
        if (player instanceof IBedRespawn bedPlayer) {
            if (bedPlayer.getBedRespawn() >= Config.Bed.MAX_BED_RESPAWNS && validRespawnPosition.isEmpty() && respawnBlockPos != null) {
                return Optional.of(Vec3d.of(respawnBlockPos));
            }
        }

        return validRespawnPosition;
    }

    private boolean findAndSetSpawnPoint(ServerPlayerEntity player, ServerWorld validServerWorld, Optional<Vec3d> validRespawnPosition, BlockPos respawnBlockPos, float respawnAngle, boolean isSpawnPointSet, boolean alive) {
        var usedRespawnAnchor = false;
        validRespawnPosition = bedRespawnCheck(player, validRespawnPosition, respawnBlockPos);

        if (validRespawnPosition.isPresent()) {
            var blockState = validServerWorld.getBlockState(respawnBlockPos);
            var usingRespawnAnchor = blockState.isOf(Blocks.RESPAWN_ANCHOR);
            var validRespawnPointVec = (Vec3d) validRespawnPosition.get();
            var validRespawnAngle = getValidRespawnAngle(blockState, usingRespawnAnchor, respawnAngle, respawnBlockPos, validRespawnPointVec);

            player.refreshPositionAndAngles(validRespawnPointVec.x, validRespawnPointVec.y, validRespawnPointVec.z, validRespawnAngle, 0.0F);
            player.setSpawnPoint(validServerWorld.getRegistryKey(), respawnBlockPos, respawnAngle, isSpawnPointSet, false);

            usedRespawnAnchor = !alive && usingRespawnAnchor;
        } else if (respawnBlockPos != null) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
        }

        return usedRespawnAnchor;
    }

    private float getValidRespawnAngle(BlockState blockState, boolean usingRespawnAnchor, float respawnAngle, BlockPos respawnBlockPos, Vec3d safeRespawnPosition) {
        if (!blockState.isIn(BlockTags.BEDS) && !usingRespawnAnchor) {
            return respawnAngle;
        } else {
            Vec3d vec3d2 = Vec3d.ofBottomCenter(respawnBlockPos).subtract(safeRespawnPosition).normalize();
            return (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d2.z, vec3d2.x) * 57.2957763671875D - 90.0D);
        }
    }

    private void placePlayerEmptyPosition(ServerWorld validServerWorld, ServerPlayerEntity serverPlayerEntity) {
        while(!validServerWorld.isSpaceEmpty(serverPlayerEntity) && serverPlayerEntity.getY() < (double) validServerWorld.getTopY()) {
            serverPlayerEntity.setPosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0D, serverPlayerEntity.getZ());
        }
    }

    private void sendPlayerRespawnPackets(ServerPlayerEntity player, boolean alive, ServerWorld validServerWorld, WorldProperties worldProperties) {
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.world.getDimension(), player.world.getRegistryKey(), BiomeAccess.hashSeed(player.getServerWorld().getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), player.getServerWorld().isDebugWorld(), player.getServerWorld().isFlat(), alive));
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(validServerWorld.getSpawnPos(), validServerWorld.getSpawnAngle()));
        player.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
    }

    private void sendWorldRespawnPackets(ServerPlayerEntity serverPlayerEntity, ServerWorld validServerWorld) {
        this.sendWorldInfo(serverPlayerEntity, validServerWorld);
        this.sendCommandTree(serverPlayerEntity);
        validServerWorld.onPlayerRespawned(serverPlayerEntity);
    }

    private void addPlayerToServerList(ServerPlayerEntity serverPlayerEntity) {
        this.players.add(serverPlayerEntity);
        this.playerMap.put(serverPlayerEntity.getUuid(), serverPlayerEntity);
    }

    private void finalSetupNewServerPlayerEntity(ServerPlayerEntity serverPlayerEntity) {
        serverPlayerEntity.onSpawn();
        serverPlayerEntity.setHealth(serverPlayerEntity.getHealth());
    }


    /**
     * Why are all these bloody functions so bloated?
     * @author EonZeNx
     * @reason Big function, add a few extra lines,
     * original functionality preserved in new functions,
     * blah blah blah
     */
    @Override
    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive) {
        removePlayerFromServerList(player);
        var respawnBlockPos = getRespawnPoint(player);
        var respawnAngle = getRespawnAngle(player);
        var isSpawnPointSet = getIsSpawnPointSet(player);
        var serverWorld = getServerWorld(player);

        var validRespawnPosition = getValidRespawnPosition(serverWorld, respawnBlockPos, respawnAngle, isSpawnPointSet, alive);
        var validServerWorld = getValidServerWorld(serverWorld, validRespawnPosition);

        var serverPlayerEntity = setupNewServerPlayerEntity(player, validServerWorld, alive);
        var usedRespawnAnchor = findAndSetSpawnPoint(serverPlayerEntity, validServerWorld, validRespawnPosition, respawnBlockPos, respawnAngle, isSpawnPointSet, alive);
        placePlayerEmptyPosition(serverWorld, serverPlayerEntity);

        var worldProperties = serverPlayerEntity.world.getLevelProperties();
        sendPlayerRespawnPackets(serverPlayerEntity, alive, validServerWorld, worldProperties);
        sendWorldRespawnPackets(serverPlayerEntity, validServerWorld);
        addPlayerToServerList(serverPlayerEntity);

        finalSetupNewServerPlayerEntity(serverPlayerEntity);

        if (usedRespawnAnchor) {
            serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double)respawnBlockPos.getX(), (double)respawnBlockPos.getY(), (double)respawnBlockPos.getZ(), 1.0F, 1.0F));
        }

        return serverPlayerEntity;
    }
}
