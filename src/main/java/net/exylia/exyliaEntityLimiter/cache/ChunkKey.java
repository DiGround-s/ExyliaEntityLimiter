package net.exylia.exyliaEntityLimiter.cache;

import lombok.Value;
import org.bukkit.Chunk;

import java.util.concurrent.ConcurrentHashMap;

@Value
public class ChunkKey {
    String world;
    int x;
    int z;

    private static final ConcurrentHashMap<ChunkKey, ChunkKey> POOL = new ConcurrentHashMap<>();
    private static final int MAX_POOL_SIZE = 10000;

    public static ChunkKey of(Chunk chunk) {
        return of(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static ChunkKey of(String world, int x, int z) {
        ChunkKey key = new ChunkKey(world, x, z);

        if (POOL.size() < MAX_POOL_SIZE) {
            return POOL.computeIfAbsent(key, k -> k);
        }

        return key;
    }

    public static void clearPool() {
        POOL.clear();
    }
}
