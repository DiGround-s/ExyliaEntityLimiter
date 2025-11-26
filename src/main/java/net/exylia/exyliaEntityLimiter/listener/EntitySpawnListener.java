package net.exylia.exyliaEntityLimiter.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.api.event.EntityLimitReachedEvent;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.cache.ChunkKey;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Map;

@Singleton
public class EntitySpawnListener implements Listener {
    private final ChunkEntityCache cache;
    private final ConfigManager configManager;

    @Inject
    public EntitySpawnListener(ChunkEntityCache cache, ConfigManager configManager) {
        this.cache = cache;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();

        if (!configManager.getConfig().hasLimit(type)) {
            return;
        }

        Chunk chunk = event.getLocation().getChunk();
        ChunkKey key = ChunkKey.of(chunk);

        Map<EntityType, Integer> counts = cache.getIfPresent(key);

        if (counts != null) {
            int current = counts.getOrDefault(type, 0);
            int limit = configManager.getConfig().getLimitValue(type);

            if (current >= limit) {
                EntityLimitReachedEvent limitEvent = new EntityLimitReachedEvent(
                        chunk,
                        type,
                        current,
                        limit
                );

                Bukkit.getPluginManager().callEvent(limitEvent);

                if (!limitEvent.isCancelled()) {
                    event.setCancelled(true);

                    if (configManager.getConfig().isDebug()) {
                        Bukkit.getLogger().info(
                                String.format("Cancelled spawn of %s in chunk [%d, %d] (limit: %d, current: %d)",
                                        type, chunk.getX(), chunk.getZ(), limit, current)
                        );
                    }
                }
                return;
            }
        }

        cache.getOrCompute(chunk);
    }
}
