package net.exylia.exyliaEntityLimiter.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.cache.ChunkEntityCache;
import net.exylia.exyliaEntityLimiter.cache.ChunkKey;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.util.ThreadPoolManager;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.concurrent.CompletableFuture;

@Singleton
public class ChunkLoadListener implements Listener {
    private final ChunkEntityCache cache;
    private final EntityLimiterService service;
    private final ThreadPoolManager threadPoolManager;

    @Inject
    public ChunkLoadListener(
            ChunkEntityCache cache,
            EntityLimiterService service,
            ThreadPoolManager threadPoolManager
    ) {
        this.cache = cache;
        this.service = service;
        this.threadPoolManager = threadPoolManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        CompletableFuture.runAsync(() -> {
            cache.getOrCompute(chunk).thenAccept(counts -> {
                service.checkChunkLimits(chunk).thenAccept(result -> {
                    if (result.hasExceeded()) {
                        service.removeExcessEntities(chunk);
                    }
                });
            });
        }, threadPoolManager.getAsyncExecutor());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        ChunkKey key = ChunkKey.of(chunk);
        cache.invalidate(key);
    }
}
