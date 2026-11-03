package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * One-controller-per-room claims with a tick expiry.
 *
 * <p>A controller refreshes its claim every tick. Two live owners of the same detected room are an
 * overlap, and both are invalidated. When a controller is broken or its chunk unloads it stops
 * refreshing, so the room becomes usable again after {@code ttl} ticks instead of keeping a stale
 * owner forever.</p>
 */
public final class RoomClaims {
    /** room id to controller position to last-refresh game time. */
    private final Map<String, Map<BlockPos, Long>> claims = new HashMap<>();

    public synchronized boolean claim(String roomId, BlockPos owner, long now, long ttl) {
        if (roomId == null || roomId.isEmpty()) return false;
        Map<BlockPos, Long> owners = claims.computeIfAbsent(roomId, key -> new HashMap<>());
        prune(owners, now, ttl);
        owners.put(owner.immutable(), now);
        return owners.size() > 1;
    }

    public synchronized int liveOwners(String roomId, long now, long ttl) {
        if (roomId == null || roomId.isEmpty()) return 0;
        Map<BlockPos, Long> owners = claims.get(roomId);
        if (owners == null) return 0;
        prune(owners, now, ttl);
        if (owners.isEmpty()) claims.remove(roomId);
        return owners.size();
    }

    public synchronized void release(String roomId, BlockPos owner) {
        if (roomId == null || roomId.isEmpty()) return;
        Map<BlockPos, Long> owners = claims.get(roomId);
        if (owners == null) return;
        owners.remove(owner);
        if (owners.isEmpty()) claims.remove(roomId);
    }

    private static void prune(Map<BlockPos, Long> owners, long now, long ttl) {
        owners.entrySet().removeIf(entry -> now - entry.getValue() > ttl);
    }
}
