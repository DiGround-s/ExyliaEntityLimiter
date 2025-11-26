package net.exylia.exyliaEntityLimiter.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.ExyliaEntityLimiter;
import net.exylia.exyliaEntityLimiter.alert.AlertManager;
import net.exylia.exyliaEntityLimiter.api.EntityLimiterAPI;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.command.subcommands.*;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.config.LimiterConfig;
import net.exylia.exyliaEntityLimiter.core.ChunkEntityTracker;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterServiceImpl;
import net.exylia.exyliaEntityLimiter.listener.ChunkLoadListener;
import net.exylia.exyliaEntityLimiter.listener.EntitySpawnListener;
import net.exylia.exyliaEntityLimiter.task.EntityCleanupTask;
import net.exylia.exyliaEntityLimiter.task.ParticleProcessorTask;
import net.exylia.exyliaEntityLimiter.task.TaskScheduler;
import net.exylia.exyliaEntityLimiter.util.EntityUtils;
import net.exylia.exyliaEntityLimiter.util.ParticleEffectUtil;
import net.exylia.exyliaEntityLimiter.util.ThreadPoolManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityLimiterModule extends AbstractModule {
    private final ExyliaEntityLimiter plugin;

    public EntityLimiterModule(ExyliaEntityLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        bind(ExyliaEntityLimiter.class).toInstance(plugin);

        bind(ConfigManager.class).in(Singleton.class);
        bind(ChunkEntityCache.class).in(Singleton.class);
        bind(ThreadPoolManager.class).in(Singleton.class);
        bind(EntityLimiterService.class).to(EntityLimiterServiceImpl.class).in(Singleton.class);
        bind(EntityLimiterAPI.class).to(EntityLimiterServiceImpl.class).in(Singleton.class);

        bind(EntityUtils.class).in(Singleton.class);
        bind(ParticleEffectUtil.class).in(Singleton.class);
        bind(ChunkEntityTracker.class).in(Singleton.class);
        bind(AlertManager.class).in(Singleton.class);

        bind(EntitySpawnListener.class).in(Singleton.class);
        bind(ChunkLoadListener.class).in(Singleton.class);

        bind(EntityCleanupTask.class).in(Singleton.class);
        bind(ParticleProcessorTask.class).in(Singleton.class);
        bind(TaskScheduler.class).in(Singleton.class);

        bind(ReloadCommand.class).in(Singleton.class);
        bind(StatsCommand.class).in(Singleton.class);
        bind(CleanupCommand.class).in(Singleton.class);
        bind(InfoCommand.class).in(Singleton.class);
        bind(ListCommand.class).in(Singleton.class);
        bind(TeleportCommand.class).in(Singleton.class);
        bind(AlertCommand.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public LimiterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig();
    }
}
