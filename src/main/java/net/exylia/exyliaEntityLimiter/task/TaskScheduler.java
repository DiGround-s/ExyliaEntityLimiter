package net.exylia.exyliaEntityLimiter.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.util.ThreadPoolManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class TaskScheduler {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ThreadPoolManager threadPoolManager;
    private final EntityCleanupTask cleanupTask;
    private final ParticleProcessorTask particleTask;

    @Getter
    private ScheduledFuture<?> cleanupFuture;

    @Getter
    private BukkitTask particleBukkitTask;

    @Inject
    public TaskScheduler(
            JavaPlugin plugin,
            ConfigManager configManager,
            ThreadPoolManager threadPoolManager,
            EntityCleanupTask cleanupTask,
            ParticleProcessorTask particleTask
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.threadPoolManager = threadPoolManager;
        this.cleanupTask = cleanupTask;
        this.particleTask = particleTask;
    }

    public void start() {
        int cleanupIntervalTicks = configManager.getConfig().getCleanupInterval();
        long cleanupIntervalMillis = cleanupIntervalTicks * 50L;

        cleanupFuture = threadPoolManager.getScheduledExecutor().scheduleAtFixedRate(
                cleanupTask,
                cleanupIntervalMillis,
                cleanupIntervalMillis,
                TimeUnit.MILLISECONDS
        );

        particleBukkitTask = particleTask.runTaskTimer(plugin, 1L, 1L);

        plugin.getLogger().info("Entity limiter tasks started (cleanup interval: " + cleanupIntervalTicks + " ticks / " + (cleanupIntervalMillis/1000.0) + "s)");
    }

    public void stop() {
        plugin.getLogger().info("[TaskScheduler] Stopping cleanup task...");
        if (cleanupFuture != null && !cleanupFuture.isCancelled()) {
            cleanupFuture.cancel(true);
            plugin.getLogger().info("[TaskScheduler] Cleanup task cancelled");
        }

        plugin.getLogger().info("[TaskScheduler] Stopping particle task...");
        if (particleBukkitTask != null && !particleBukkitTask.isCancelled()) {
            particleBukkitTask.cancel();
            plugin.getLogger().info("[TaskScheduler] Particle task cancelled");
        }

        plugin.getLogger().info("[TaskScheduler] All tasks stopped");
    }

    public void restart() {
        stop();
        start();
    }
}
