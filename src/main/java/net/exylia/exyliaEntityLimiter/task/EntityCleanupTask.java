package net.exylia.exyliaEntityLimiter.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.core.ChunkEntityTracker;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.util.ThreadPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class EntityCleanupTask implements Runnable {
    private final EntityLimiterService service;
    private final ConfigManager configManager;
    private final ThreadPoolManager threadPoolManager;

    @Inject
    public EntityCleanupTask(
            EntityLimiterService service,
            ConfigManager configManager,
            ThreadPoolManager threadPoolManager
    ) {
        this.service = service;
        this.configManager = configManager;
        this.threadPoolManager = threadPoolManager;
    }

    @Override
    public void run() {
        try {
            boolean debug = configManager.getConfig().isDebug();

            if (debug) {
                Bukkit.getLogger().info("[CleanupTask] Starting periodic cleanup cycle");
            }

            List<Chunk> loadedChunks = new ArrayList<>();

            for (World world : Bukkit.getWorlds()) {
                loadedChunks.addAll(Arrays.asList(world.getLoadedChunks()));
            }

            if (debug) {
                Bukkit.getLogger().info("[CleanupTask] Processing " + loadedChunks.size() + " loaded chunks");
            }

            if (loadedChunks.isEmpty()) {
                if (debug) {
                    Bukkit.getLogger().info("[CleanupTask] No loaded chunks, skipping");
                }
                return;
            }

            int processed = 0;
            for (Chunk chunk : loadedChunks) {
                final int chunkNum = ++processed;
                service.checkChunkLimits(chunk)
                        .thenCompose(result -> {
                            if (result.hasExceeded()) {
                                if (debug) {
                                    Bukkit.getLogger().info(
                                            String.format("[CleanupTask] Chunk [%d, %d] exceeded limits (chunk %d/%d)",
                                                    chunk.getX(), chunk.getZ(), chunkNum, loadedChunks.size())
                                    );
                                }
                                return service.removeExcessEntities(chunk)
                                        .thenAccept(removed -> {
                                            if (removed > 0) {
                                                Bukkit.getLogger().info(
                                                        String.format("[CleanupTask] Removed %d entities from chunk [%d, %d]",
                                                                removed, chunk.getX(), chunk.getZ())
                                                );
                                            }
                                        });
                            }
                            return CompletableFuture.completedFuture(null);
                        })
                        .exceptionally(ex -> {
                            Bukkit.getLogger().warning("[CleanupTask] Error during chunk cleanup: " + ex.getMessage());
                            if (debug) {
                                ex.printStackTrace();
                            }
                            return null;
                        });
            }

            if (debug) {
                Bukkit.getLogger().info("[CleanupTask] Cleanup cycle completed, processed " + processed + " chunks");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[CleanupTask] Critical error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
