package net.exylia.exyliaEntityLimiter.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Singleton
public class ConfigManager {
    private final JavaPlugin plugin;

    @Getter
    private LimiterConfig config;

    @Inject
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration file = plugin.getConfig();

        int cleanupInterval = file.getInt("entity-limiter.cleanup-interval", 100);
        boolean debug = file.getBoolean("entity-limiter.debug", false);

        String strategyStr = file.getString("entity-limiter.removal-strategy", "FARTHEST_FROM_PLAYERS");
        LimiterConfig.RemovalStrategyType removalStrategy;
        try {
            removalStrategy = LimiterConfig.RemovalStrategyType.valueOf(strategyStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid removal strategy: " + strategyStr + ", using FARTHEST_FROM_PLAYERS");
            removalStrategy = LimiterConfig.RemovalStrategyType.FARTHEST_FROM_PLAYERS;
        }

        boolean particleEnabled = file.getBoolean("entity-limiter.particle.enabled", true);
        String particleTypeStr = file.getString("entity-limiter.particle.type", "SMOKE");
        Particle particleType;
        try {
            particleType = Particle.valueOf(particleTypeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + particleTypeStr + ", using SMOKE");
            particleType = Particle.SMOKE;
        }
        int particleCount = file.getInt("entity-limiter.particle.count", 10);

        boolean bypassCustomNamed = file.getBoolean("entity-limiter.bypass.custom-named", true);
        boolean bypassVillagersWithProfession = file.getBoolean("entity-limiter.bypass.villagers-with-profession", true);
        boolean bypassTaggedEntities = file.getBoolean("entity-limiter.bypass.bypass-tagged-entities", true);

        Map<EntityType, EntityLimitConfig> limits = new EnumMap<>(EntityType.class);
        ConfigurationSection limitsSection = file.getConfigurationSection("limits");
        if (limitsSection != null) {
            for (String key : limitsSection.getKeys(false)) {
                if (key.equals("default")) continue;

                try {
                    EntityType type = EntityType.valueOf(key);
                    int limit = limitsSection.getInt(key);
                    limits.put(type, new EntityLimitConfig(limit));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid entity type in config: " + key);
                }
            }
        }

        int defaultLimitValue = file.getInt("limits.default", -1);
        EntityLimitConfig defaultLimit = new EntityLimitConfig(defaultLimitValue);

        int chunkTtlSeconds = file.getInt("cache.chunk-ttl-seconds", 5);
        int chunkMaxSize = file.getInt("cache.chunk-max-size", 1000);
        int bypassTtlSeconds = file.getInt("cache.bypass-ttl-seconds", 30);
        int bypassMaxSize = file.getInt("cache.bypass-max-size", 5000);

        int corePoolSize = file.getInt("threading.core-pool-size", 2);
        int maxPoolSize = file.getInt("threading.max-pool-size", 4);

        config = LimiterConfig.builder()
                .cleanupInterval(cleanupInterval)
                .debug(debug)
                .removalStrategy(removalStrategy)
                .particleEnabled(particleEnabled)
                .particleType(particleType)
                .particleCount(particleCount)
                .bypassCustomNamed(bypassCustomNamed)
                .bypassVillagersWithProfession(bypassVillagersWithProfession)
                .bypassTaggedEntities(bypassTaggedEntities)
                .limits(limits)
                .defaultLimit(defaultLimit)
                .chunkTtlSeconds(chunkTtlSeconds)
                .chunkMaxSize(chunkMaxSize)
                .bypassTtlSeconds(bypassTtlSeconds)
                .bypassMaxSize(bypassMaxSize)
                .corePoolSize(corePoolSize)
                .maxPoolSize(maxPoolSize)
                .build();

        if (debug) {
            plugin.getLogger().info("Configuration loaded: " + limits.size() + " entity types configured");
        }
    }
}
