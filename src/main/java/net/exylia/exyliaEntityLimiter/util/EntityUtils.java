package net.exylia.exyliaEntityLimiter.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.config.LimiterConfig;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Singleton
public class EntityUtils {
    private final ChunkEntityCache cache;
    private final ConfigManager configManager;
    private final List<Predicate<Entity>> customBypassPredicates;

    @Inject
    public EntityUtils(ChunkEntityCache cache, ConfigManager configManager) {
        this.cache = cache;
        this.configManager = configManager;
        this.customBypassPredicates = new ArrayList<>();
    }

    public boolean shouldBypassEntity(Entity entity) {
        UUID uuid = entity.getUniqueId();
        Boolean cached = cache.getBypassStatus(uuid);

        if (cached != null && cached) {
            return true;
        }

        boolean bypass = computeBypassStatus(entity);
        cache.putBypassStatus(uuid, bypass);

        return bypass;
    }

    private boolean computeBypassStatus(Entity entity) {
        LimiterConfig config = configManager.getConfig();

        if (config.isBypassCustomNamed() && entity.customName() != null) {
            return true;
        }

        if (entity instanceof Villager villager) {
            if (config.isBypassVillagersWithProfession() &&
                    villager.getProfession() != Villager.Profession.NONE) {
                return true;
            }
        }

        if (config.isBypassTaggedEntities() && !entity.getScoreboardTags().isEmpty()) {
            return true;
        }

        for (Predicate<Entity> predicate : customBypassPredicates) {
            if (predicate.test(entity)) {
                return true;
            }
        }

        return false;
    }

    public void registerBypassPredicate(Predicate<Entity> predicate) {
        customBypassPredicates.add(predicate);
    }

    public void clearBypassPredicates() {
        customBypassPredicates.clear();
    }
}
