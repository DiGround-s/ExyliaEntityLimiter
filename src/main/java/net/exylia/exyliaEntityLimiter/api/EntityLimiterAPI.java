package net.exylia.exyliaEntityLimiter.api;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface EntityLimiterAPI {
    int getLimit(EntityType type);

    CompletableFuture<Boolean> hasReachedLimit(Chunk chunk, EntityType type);

    CompletableFuture<Map<EntityType, Integer>> getEntityCount(Chunk chunk);

    void registerBypassPredicate(Predicate<Entity> predicate);

    CompletableFuture<Integer> cleanupChunk(Chunk chunk);

    CacheStats getChunkCacheStats();

    CacheStats getBypassCacheStats();

    void invalidateCache(Chunk chunk);

    void clearAllCaches();
}
