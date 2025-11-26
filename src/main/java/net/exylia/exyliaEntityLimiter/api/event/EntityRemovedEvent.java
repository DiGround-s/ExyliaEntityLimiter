package net.exylia.exyliaEntityLimiter.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class EntityRemovedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Chunk chunk;
    private final List<Entity> removedEntities;
    private final RemovalReason reason;

    public EntityRemovedEvent(Chunk chunk, List<Entity> removedEntities, RemovalReason reason) {
        super(true);
        this.chunk = chunk;
        this.removedEntities = List.copyOf(removedEntities);
        this.reason = reason;
    }

    public int getRemovedCount() {
        return removedEntities.size();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @RequiredArgsConstructor
    @Getter
    public enum RemovalReason {
        LIMIT_REACHED("Limit reached"),
        MANUAL("Manual cleanup"),
        PERIODIC_CLEANUP("Periodic cleanup");

        private final String description;
    }
}
