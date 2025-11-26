package net.exylia.exyliaEntityLimiter.core;

import lombok.Value;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;

import java.util.Map;

@Value
public class ChunkEntityData implements Comparable<ChunkEntityData> {
    Chunk chunk;
    Map<EntityType, Integer> entityCounts;
    int totalEntities;
    int excessEntities;
    boolean hasExceeded;

    @Override
    public int compareTo(ChunkEntityData other) {
        if (this.hasExceeded != other.hasExceeded) {
            return this.hasExceeded ? -1 : 1;
        }
        return Integer.compare(other.totalEntities, this.totalEntities);
    }

    public String getWorldName() {
        return chunk.getWorld().getName();
    }

    public int getX() {
        return chunk.getX();
    }

    public int getZ() {
        return chunk.getZ();
    }
}
