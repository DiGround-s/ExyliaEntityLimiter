package net.exylia.exyliaEntityLimiter.core;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.alert.AlertManager;
import net.exylia.exyliaEntityLimiter.api.event.EntityRemovedEvent;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.cache.ChunkKey;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.util.EntityUtils;
import net.exylia.exyliaEntityLimiter.util.ParticleEffectUtil;
import net.exylia.exyliaEntityLimiter.util.ThreadPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Singleton
public class EntityLimiterServiceImpl implements EntityLimiterService {
    private final JavaPlugin plugin;
    private final ChunkEntityCache cache;
    private final ChunkEntityTracker tracker;
    private final ConfigManager configManager;
    private final EntityUtils entityUtils;
    private final ThreadPoolManager threadPoolManager;
    private final ParticleEffectUtil particleUtil;
    private final AlertManager alertManager;

    @Inject
    public EntityLimiterServiceImpl(
            JavaPlugin plugin,
            ChunkEntityCache cache,
            ChunkEntityTracker tracker,
            ConfigManager configManager,
            EntityUtils entityUtils,
            ThreadPoolManager threadPoolManager,
            ParticleEffectUtil particleUtil,
            AlertManager alertManager
    ) {
        this.plugin = plugin;
        this.cache = cache;
        this.tracker = tracker;
        this.configManager = configManager;
        this.entityUtils = entityUtils;
        this.threadPoolManager = threadPoolManager;
        this.particleUtil = particleUtil;
        this.alertManager = alertManager;
    }

    @Override
    public CompletableFuture<ChunkEntityTracker.LimitCheckResult> checkChunkLimits(Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> tracker.checkLimits(chunk), threadPoolManager.getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Integer> removeExcessEntities(Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkEntityTracker.LimitCheckResult result = tracker.checkLimits(chunk);
            if (!result.hasExceeded()) {
                return 0;
            }

            int totalRemoved = 0;
            for (ChunkEntityTracker.LimitExceeded exceeded : result.getExceeded().values()) {
                int removed = removeExcessEntitiesSync(chunk, exceeded.getType(), exceeded.getExcess());
                totalRemoved += removed;
            }

            return totalRemoved;
        }, threadPoolManager.getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Integer> removeExcessEntities(Chunk chunk, EntityType type, int count) {
        return CompletableFuture.supplyAsync(
                () -> removeExcessEntitiesSync(chunk, type, count),
                threadPoolManager.getAsyncExecutor()
        );
    }

    private int removeExcessEntitiesSync(Chunk chunk, EntityType type, int toRemove) {
        List<Entity> candidates = tracker.getRemovalCandidates(chunk, type, toRemove);

        if (candidates.isEmpty()) {
            return 0;
        }

        EntityRemovalStrategy strategy = EntityRemovalStrategy.fromConfig(
                configManager.getConfig().getRemovalStrategy()
        );

        List<Entity> sorted = strategy.sortForRemoval(candidates, chunk);
        List<Entity> toRemoveList = sorted.stream()
                .limit(toRemove)
                .toList();

        if (!plugin.isEnabled()) {
            return 0;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            List<Entity> removed = new ArrayList<>();
            Map<EntityType, Integer> removedByType = new HashMap<>();

            for (Entity entity : toRemoveList) {
                if (entity.isValid() && !entity.isDead()) {
                    if (configManager.getConfig().isParticleEnabled()) {
                        particleUtil.queueParticle(
                                entity.getLocation(),
                                configManager.getConfig().getParticleType(),
                                configManager.getConfig().getParticleCount()
                        );
                    }

                    EntityType entityType = entity.getType();
                    entity.remove();
                    removed.add(entity);
                    removedByType.merge(entityType, 1, Integer::sum);
                }
            }

            if (!removed.isEmpty()) {
                EntityRemovedEvent event = new EntityRemovedEvent(
                        chunk,
                        removed,
                        EntityRemovedEvent.RemovalReason.LIMIT_REACHED
                );
                Bukkit.getPluginManager().callEvent(event);

                alertManager.sendRemovalAlert(chunk, removedByType);
            }

            invalidateCache(chunk);
        });

        return toRemoveList.size();
    }

    @Override
    public void warmCache(Chunk chunk) {
        CompletableFuture.runAsync(() -> {
            cache.getOrCompute(chunk);
        }, threadPoolManager.getAsyncExecutor());
    }

    @Override
    public int getLimit(EntityType type) {
        return configManager.getConfig().getLimitValue(type);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedLimit(Chunk chunk, EntityType type) {
        return CompletableFuture.supplyAsync(() -> {
            Map<EntityType, Integer> counts = tracker.getEntityCounts(chunk);
            int current = counts.getOrDefault(type, 0);
            int limit = getLimit(type);

            return limit >= 0 && current >= limit;
        }, threadPoolManager.getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Map<EntityType, Integer>> getEntityCount(Chunk chunk) {
        return CompletableFuture.supplyAsync(
                () -> tracker.getEntityCounts(chunk),
                threadPoolManager.getAsyncExecutor()
        );
    }

    @Override
    public void registerBypassPredicate(Predicate<Entity> predicate) {
        entityUtils.registerBypassPredicate(predicate);
    }

    @Override
    public CompletableFuture<Integer> cleanupChunk(Chunk chunk) {
        return removeExcessEntities(chunk);
    }

    @Override
    public CacheStats getChunkCacheStats() {
        return cache.getChunkCacheStats();
    }

    @Override
    public CacheStats getBypassCacheStats() {
        return cache.getBypassCacheStats();
    }

    @Override
    public void invalidateCache(Chunk chunk) {
        cache.invalidate(ChunkKey.of(chunk));
    }

    @Override
    public void clearAllCaches() {
        cache.clear();
    }
}
