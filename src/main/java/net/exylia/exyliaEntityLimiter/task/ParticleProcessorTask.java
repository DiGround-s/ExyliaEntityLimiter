package net.exylia.exyliaEntityLimiter.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.util.ParticleEffectUtil;
import org.bukkit.scheduler.BukkitRunnable;

@Singleton
public class ParticleProcessorTask extends BukkitRunnable {
    private final ParticleEffectUtil particleUtil;

    @Inject
    public ParticleProcessorTask(ParticleEffectUtil particleUtil) {
        this.particleUtil = particleUtil;
    }

    @Override
    public void run() {
        particleUtil.processQueue();
    }
}
