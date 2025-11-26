package net.exylia.exyliaEntityLimiter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import net.exylia.exyliaEntityLimiter.api.EntityLimiterAPI;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.cache.ChunkKey;
import net.exylia.exyliaEntityLimiter.command.EntityLimiterCommand;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.inject.EntityLimiterModule;
import net.exylia.exyliaEntityLimiter.listener.ChunkLoadListener;
import net.exylia.exyliaEntityLimiter.listener.EntitySpawnListener;
import net.exylia.exyliaEntityLimiter.task.TaskScheduler;
import net.exylia.exyliaEntityLimiter.util.ParticleEffectUtil;
import net.exylia.exyliaEntityLimiter.util.ThreadPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExyliaEntityLimiter extends JavaPlugin {
    @Getter
    private static ExyliaEntityLimiter instance;

    private Injector injector;
    private EntityLimiterService service;
    private TaskScheduler taskScheduler;
    private ThreadPoolManager threadPoolManager;
    private ChunkEntityCache cache;
    private ParticleEffectUtil particleUtil;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("=== EntityLimiter Startup ===");
        getLogger().info("[1/8] Creating Guice injector...");

        injector = Guice.createInjector(new EntityLimiterModule(this));
        getLogger().info("[2/8] Guice injector created");

        getLogger().info("[3/8] Loading configuration...");
        ConfigManager configManager = injector.getInstance(ConfigManager.class);
        getLogger().info("[3/8] Configuration loaded (debug: " + configManager.getConfig().isDebug() + ")");

        getLogger().info("[4/8] Initializing services...");
        service = injector.getInstance(EntityLimiterService.class);
        taskScheduler = injector.getInstance(TaskScheduler.class);
        threadPoolManager = injector.getInstance(ThreadPoolManager.class);
        cache = injector.getInstance(ChunkEntityCache.class);
        particleUtil = injector.getInstance(ParticleEffectUtil.class);
        getLogger().info("[4/8] Services initialized");

        getLogger().info("[5/8] Registering listeners...");
        registerListeners();
        getLogger().info("[5/8] Listeners registered");

        getLogger().info("[6/8] Registering commands...");
        registerCommands();
        getLogger().info("[6/8] Commands registered");

        getLogger().info("[7/8] Registering API...");
        registerAPI();
        getLogger().info("[7/8] API registered");

        getLogger().info("[8/8] Starting tasks...");
        taskScheduler.start();
        getLogger().info("[8/8] Tasks started");

        getLogger().info("=== EntityLimiter Enabled ===");
        getLogger().info("Chunk-based limiting with " + configManager.getConfig().getRemovalStrategy() + " removal strategy");
        getLogger().info("Cleanup interval: " + configManager.getConfig().getCleanupInterval() + " ticks");
    }

    @Override
    public void onDisable() {
        getLogger().info("=== EntityLimiter Shutdown ===");

        getLogger().info("[1/5] Stopping tasks...");
        if (taskScheduler != null) {
            try {
                taskScheduler.stop();
                getLogger().info("[1/5] Tasks stopped successfully");
            } catch (Exception e) {
                getLogger().warning("[1/5] Error stopping tasks: " + e.getMessage());
            }
        }

        getLogger().info("[2/5] Shutting down thread pools...");
        if (threadPoolManager != null) {
            try {
                threadPoolManager.shutdown();
                getLogger().info("[2/5] Thread pools shutdown initiated");
            } catch (Exception e) {
                getLogger().warning("[2/5] Error shutting down thread pools: " + e.getMessage());
            }
        }

        getLogger().info("[3/5] Clearing caches...");
        if (cache != null) {
            try {
                cache.clear();
                getLogger().info("[3/5] Caches cleared");
            } catch (Exception e) {
                getLogger().warning("[3/5] Error clearing caches: " + e.getMessage());
            }
        }

        getLogger().info("[4/5] Clearing particle queue...");
        if (particleUtil != null) {
            try {
                particleUtil.clearQueue();
                getLogger().info("[4/5] Particle queue cleared");
            } catch (Exception e) {
                getLogger().warning("[4/5] Error clearing particle queue: " + e.getMessage());
            }
        }

        getLogger().info("[5/5] Clearing chunk key pool...");
        try {
            ChunkKey.clearPool();
            getLogger().info("[5/5] Chunk key pool cleared");
        } catch (Exception e) {
            getLogger().warning("[5/5] Error clearing chunk key pool: " + e.getMessage());
        }

        getLogger().info("=== EntityLimiter Disabled ===");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(injector.getInstance(EntitySpawnListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getInstance(ChunkLoadListener.class), this);
    }

    private void registerCommands() {
        new EntityLimiterCommand(this, injector);
    }

    private void registerAPI() {
        getServer().getServicesManager().register(
                EntityLimiterAPI.class,
                service,
                this,
                ServicePriority.Normal
        );
    }

    public EntityLimiterAPI getAPI() {
        return service;
    }
}
