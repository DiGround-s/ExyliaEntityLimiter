package net.exylia.exyliaEntityLimiter.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.config.LimiterConfig;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class ChunkEntityCache {
    private final Cache<ChunkKey, Map<EntityType, Integer>> chunkCache;
    private final LoadingCache<UUID, Boolean> bypassCache;
    private final LimiterConfig config;

    @Inject
    public ChunkEntityCache(ConfigManager configManager) {
        this.config = configManager.getConfig();

        this.chunkCache = Caffeine.newBuilder()
                .expireAfterWrite(config.getChunkTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(config.getChunkMaxSize())
                .recordStats()
                .build();

        this.bypassCache = Caffeine.newBuilder()
                .expireAfterWrite(config.getBypassTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(config.getBypassMaxSize())
                .recordStats()
                .build(key -> false);
    }

    public Map<EntityType, Integer> getIfPresent(ChunkKey key) {
        return chunkCache.getIfPresent(key);
    }

    public void put(ChunkKey key, Map<EntityType, Integer> counts) {
        chunkCache.put(key, counts);
    }

    public void invalidate(ChunkKey key) {
        chunkCache.invalidate(key);
    }

    public void invalidateAll() {
        chunkCache.invalidateAll();
    }

    public Map<EntityType, Integer> countEntities(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        Map<EntityType, Integer> counts = new EnumMap<>(EntityType.class);

        for (Entity entity : entities) {
            EntityType type = entity.getType();
            counts.merge(type, 1, Integer::sum);
        }

        return counts;
    }

    public CompletableFuture<Map<EntityType, Integer>> getOrCompute(Chunk chunk) {
        ChunkKey key = ChunkKey.of(chunk);
        Map<EntityType, Integer> cached = chunkCache.getIfPresent(key);

        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            Map<EntityType, Integer> counts = countEntities(chunk);
            chunkCache.put(key, counts);
            return counts;
        });
    }

    public Boolean getBypassStatus(UUID entityId) {
        return bypassCache.get(entityId);
    }

    public void putBypassStatus(UUID entityId, boolean bypass) {
        bypassCache.put(entityId, bypass);
    }

    public void invalidateBypass(UUID entityId) {
        bypassCache.invalidate(entityId);
    }

    public CacheStats getChunkCacheStats() {
        return chunkCache.stats();
    }

    public CacheStats getBypassCacheStats() {
        return bypassCache.stats();
    }

    public void clear() {
        chunkCache.invalidateAll();
        bypassCache.invalidateAll();
    }
}
