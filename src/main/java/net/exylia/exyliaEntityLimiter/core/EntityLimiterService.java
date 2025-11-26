package net.exylia.exyliaEntityLimiter.core;

import net.exylia.exyliaEntityLimiter.api.EntityLimiterAPI;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EntityLimiterService extends EntityLimiterAPI {
    CompletableFuture<ChunkEntityTracker.LimitCheckResult> checkChunkLimits(Chunk chunk);

    CompletableFuture<Integer> removeExcessEntities(Chunk chunk);

    CompletableFuture<Integer> removeExcessEntities(Chunk chunk, EntityType type, int count);

    void warmCache(Chunk chunk);
}
