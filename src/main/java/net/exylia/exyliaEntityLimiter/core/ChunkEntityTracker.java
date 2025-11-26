package net.exylia.exyliaEntityLimiter.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Value;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.cache.ChunkKey;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.util.EntityUtils;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ChunkEntityTracker {
    private final ChunkEntityCache cache;
    private final ConfigManager configManager;
    private final EntityUtils entityUtils;

    @Inject
    public ChunkEntityTracker(ChunkEntityCache cache, ConfigManager configManager, EntityUtils entityUtils) {
        this.cache = cache;
        this.configManager = configManager;
        this.entityUtils = entityUtils;
    }

    public Map<EntityType, Integer> getEntityCounts(Chunk chunk) {
        ChunkKey key = ChunkKey.of(chunk);
        Map<EntityType, Integer> cached = cache.getIfPresent(key);

        if (cached != null) {
            return new EnumMap<>(cached);
        }

        return cache.countEntities(chunk);
    }

    public Map<EntityType, List<Entity>> groupEntitiesByType(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        Map<EntityType, List<Entity>> grouped = new EnumMap<>(EntityType.class);

        for (Entity entity : entities) {
            grouped.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
        }

        return grouped;
    }

    public LimitCheckResult checkLimits(Chunk chunk) {
        Map<EntityType, Integer> counts = getEntityCounts(chunk);
        Map<EntityType, LimitExceeded> exceeded = new EnumMap<>(EntityType.class);

        counts.forEach((type, count) -> {
            int limit = configManager.getConfig().getLimitValue(type);
            if (limit >= 0 && count > limit) {
                exceeded.put(type, new LimitExceeded(type, count, limit, count - limit));
            }
        });

        return new LimitCheckResult(exceeded);
    }

    public List<Entity> getRemovalCandidates(Chunk chunk, EntityType type, int toRemove) {
        Entity[] allEntities = chunk.getEntities();
        List<Entity> candidates = new ArrayList<>();

        for (Entity entity : allEntities) {
            if (entity.getType() == type && !entityUtils.shouldBypassEntity(entity)) {
                candidates.add(entity);
            }
        }

        return candidates;
    }

    @Value
    public static class LimitCheckResult {
        Map<EntityType, LimitExceeded> exceeded;

        public boolean hasExceeded() {
            return !exceeded.isEmpty();
        }

        public boolean isExceeded(EntityType type) {
            return exceeded.containsKey(type);
        }

        public int getTotalExcess() {
            return exceeded.values().stream()
                    .mapToInt(LimitExceeded::getExcess)
                    .sum();
        }
    }

    @Value
    public static class LimitExceeded {
        EntityType type;
        int current;
        int limit;
        int excess;
    }
}
