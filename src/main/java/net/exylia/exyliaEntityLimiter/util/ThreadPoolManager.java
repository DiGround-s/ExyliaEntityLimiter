package net.exylia.exyliaEntityLimiter.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.config.LimiterConfig;

import java.util.concurrent.*;

@Singleton
public class ThreadPoolManager {
    @Getter
    private final ForkJoinPool forkJoinPool;

    @Getter
    private final ScheduledExecutorService scheduledExecutor;

    @Getter
    private final ExecutorService asyncExecutor;

    @Inject
    public ThreadPoolManager(ConfigManager configManager) {
        LimiterConfig config = configManager.getConfig();

        this.forkJoinPool = new ForkJoinPool(
                config.getCorePoolSize(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true
        );

        this.scheduledExecutor = Executors.newScheduledThreadPool(
                1,
                r -> {
                    Thread thread = new Thread(r, "EntityLimiter-Scheduler");
                    thread.setDaemon(true);
                    return thread;
                }
        );

        this.asyncExecutor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread thread = new Thread(r, "EntityLimiter-Async");
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }

    public void shutdown() {
        System.out.println("[ThreadPoolManager] Initiating shutdown...");

        System.out.println("[ThreadPoolManager] Shutting down scheduled executor...");
        scheduledExecutor.shutdownNow();

        System.out.println("[ThreadPoolManager] Shutting down fork join pool...");
        forkJoinPool.shutdownNow();

        System.out.println("[ThreadPoolManager] Shutting down async executor...");
        asyncExecutor.shutdownNow();

        System.out.println("[ThreadPoolManager] All executors shutdown initiated (non-blocking)");

        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[ThreadPoolManager] Awaiting scheduled executor termination...");
                if (scheduledExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.out.println("[ThreadPoolManager] Scheduled executor terminated");
                } else {
                    System.out.println("[ThreadPoolManager] Scheduled executor termination timeout");
                }

                System.out.println("[ThreadPoolManager] Awaiting fork join pool termination...");
                if (forkJoinPool.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.out.println("[ThreadPoolManager] Fork join pool terminated");
                } else {
                    System.out.println("[ThreadPoolManager] Fork join pool termination timeout");
                }

                System.out.println("[ThreadPoolManager] Awaiting async executor termination...");
                if (asyncExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.out.println("[ThreadPoolManager] Async executor terminated");
                } else {
                    System.out.println("[ThreadPoolManager] Async executor termination timeout");
                }

                System.out.println("[ThreadPoolManager] Shutdown complete");
            } catch (InterruptedException e) {
                System.out.println("[ThreadPoolManager] Shutdown interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }
}
