package net.exylia.exyliaEntityLimiter.util;

import com.google.inject.Singleton;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class ParticleEffectUtil {
    private static final int MAX_PARTICLES_PER_TICK = 100;
    private final Queue<ParticleRequest> particleQueue = new ConcurrentLinkedQueue<>();

    public void queueParticle(Location location, Particle type, int count) {
        particleQueue.offer(new ParticleRequest(location.clone(), type, count));
    }

    public void processQueue() {
        int spawned = 0;
        ParticleRequest request;

        while (spawned < MAX_PARTICLES_PER_TICK && (request = particleQueue.poll()) != null) {
            if (request.location.getWorld() != null) {
                request.location.getWorld().spawnParticle(
                        request.type,
                        request.location,
                        request.count,
                        0.5, 0.5, 0.5,
                        0.01
                );
                spawned += request.count;
            }
        }
    }

    public int getQueueSize() {
        return particleQueue.size();
    }

    public void clearQueue() {
        particleQueue.clear();
    }

    @Value
    private static class ParticleRequest {
        Location location;
        Particle type;
        int count;
    }
}
