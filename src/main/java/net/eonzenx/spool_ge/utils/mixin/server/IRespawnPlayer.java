package net.eonzenx.spool_ge.utils.mixin.server;

import net.minecraft.server.network.ServerPlayerEntity;

public interface IRespawnPlayer
{
    ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive);
}
