package net.exylia.exyliaEntityLimiter.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EntityLimitReachedEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Chunk chunk;
    private final EntityType entityType;
    private final int currentCount;
    private final int limit;

    @Setter
    private boolean cancelled;

    public EntityLimitReachedEvent(Chunk chunk, EntityType entityType, int currentCount, int limit) {
        super(true);
        this.chunk = chunk;
        this.entityType = entityType;
        this.currentCount = currentCount;
        this.limit = limit;
        this.cancelled = false;
    }

    public int getExcess() {
        return currentCount - limit;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
