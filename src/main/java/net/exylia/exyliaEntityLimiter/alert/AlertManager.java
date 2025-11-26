package net.exylia.exyliaEntityLimiter.alert;

import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class AlertManager {
    @Getter
    private final Set<UUID> alertEnabled = ConcurrentHashMap.newKeySet();

    public void enableAlerts(Player player) {
        alertEnabled.add(player.getUniqueId());
    }

    public void disableAlerts(Player player) {
        alertEnabled.remove(player.getUniqueId());
    }

    public boolean hasAlertsEnabled(Player player) {
        return alertEnabled.contains(player.getUniqueId());
    }

    public void toggleAlerts(Player player) {
        if (hasAlertsEnabled(player)) {
            disableAlerts(player);
        } else {
            enableAlerts(player);
        }
    }

    public void sendRemovalAlert(Chunk chunk, Map<EntityType, Integer> removedEntities) {
        if (removedEntities.isEmpty()) return;

        int totalRemoved = removedEntities.values().stream().mapToInt(Integer::intValue).sum();

        for (UUID uuid : alertEnabled) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(MessageUtil.warning("Entity cleanup triggered!"));

                Component chunkInfo = MessageUtil.parse("  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Chunk:</#59a4ff> ")
                    .append(MessageUtil.clickableChunk(chunk, "teleport"));
                player.sendMessage(chunkInfo);

                MessageUtil.send(player, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Total Removed:</#59a4ff> <#a33b53><bold>" + totalRemoved + "</bold></#a33b53>");

                removedEntities.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(5)
                    .forEach(entry -> {
                        String typeName = formatEntityType(entry.getKey());
                        MessageUtil.send(player, "    <#a89ab5>•</#a89ab5> <#e7cfff>" + typeName + ":</#e7cfff> <#a33b53>" + entry.getValue() + "</#a33b53>");
                    });
            }
        }
    }

    private String formatEntityType(EntityType type) {
        String name = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
