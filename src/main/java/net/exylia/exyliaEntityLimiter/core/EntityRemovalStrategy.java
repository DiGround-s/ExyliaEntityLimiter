package net.exylia.exyliaEntityLimiter.core;

import lombok.RequiredArgsConstructor;
import net.exylia.exyliaEntityLimiter.config.LimiterConfig;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum EntityRemovalStrategy {
    FARTHEST_FROM_PLAYERS((entities, chunk) -> {
        List<Player> nearbyPlayers = chunk.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(chunk.getBlock(8, 64, 8).getLocation()) < 256)
                .collect(Collectors.toList());

        if (nearbyPlayers.isEmpty()) {
            return entities;
        }

        return entities.stream()
                .sorted(Comparator.comparingDouble(entity -> {
                    double minDistance = Double.MAX_VALUE;
                    for (Player player : nearbyPlayers) {
                        double distance = entity.getLocation().distanceSquared(player.getLocation());
                        if (distance < minDistance) {
                            minDistance = distance;
                        }
                    }
                    return -minDistance;
                }))
                .collect(Collectors.toList());
    }),

    OLDEST_FIRST((entities, chunk) ->
            entities.stream()
                    .sorted(Comparator.comparingInt(Entity::getTicksLived).reversed())
                    .collect(Collectors.toList())
    ),

    RANDOM((entities, chunk) -> {
        List<Entity> shuffled = entities.stream().collect(Collectors.toList());
        java.util.Collections.shuffle(shuffled, new Random());
        return shuffled;
    }),

    LOWEST_HEALTH((entities, chunk) ->
            entities.stream()
                    .sorted((e1, e2) -> {
                        if (e1 instanceof LivingEntity living1 && e2 instanceof LivingEntity living2) {
                            return Double.compare(living1.getHealth(), living2.getHealth());
                        }
                        if (e1 instanceof LivingEntity) return 1;
                        if (e2 instanceof LivingEntity) return -1;
                        return 0;
                    })
                    .collect(Collectors.toList())
    );

    private final StrategyFunction function;

    public List<Entity> sortForRemoval(List<Entity> entities, Chunk chunk) {
        return function.apply(entities, chunk);
    }

    public static EntityRemovalStrategy fromConfig(LimiterConfig.RemovalStrategyType type) {
        return switch (type) {
            case FARTHEST_FROM_PLAYERS -> FARTHEST_FROM_PLAYERS;
            case OLDEST_FIRST -> OLDEST_FIRST;
            case RANDOM -> RANDOM;
            case LOWEST_HEALTH -> LOWEST_HEALTH;
        };
    }

    @FunctionalInterface
    private interface StrategyFunction {
        List<Entity> apply(List<Entity> entities, Chunk chunk);
    }
}
