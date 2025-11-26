package net.exylia.exyliaEntityLimiter.config;

import lombok.Builder;
import lombok.Value;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class LimiterConfig {
    int cleanupInterval;
    boolean debug;
    RemovalStrategyType removalStrategy;

    boolean particleEnabled;
    Particle particleType;
    int particleCount;

    boolean bypassCustomNamed;
    boolean bypassVillagersWithProfession;
    boolean bypassTaggedEntities;

    Map<EntityType, EntityLimitConfig> limits;
    EntityLimitConfig defaultLimit;

    int chunkTtlSeconds;
    int chunkMaxSize;
    int bypassTtlSeconds;
    int bypassMaxSize;

    int corePoolSize;
    int maxPoolSize;

    public enum RemovalStrategyType {
        FARTHEST_FROM_PLAYERS,
        OLDEST_FIRST,
        RANDOM,
        LOWEST_HEALTH
    }

    public EntityLimitConfig getLimit(EntityType type) {
        return limits.getOrDefault(type, defaultLimit);
    }

    public boolean hasLimit(EntityType type) {
        EntityLimitConfig config = getLimit(type);
        return config.hasLimit();
    }

    public int getLimitValue(EntityType type) {
        return getLimit(type).getLimit();
    }
}
