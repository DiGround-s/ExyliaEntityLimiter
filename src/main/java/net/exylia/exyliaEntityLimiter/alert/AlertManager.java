package net.exylia.exyliaEntityLimiter.alert;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class AlertManager {
    private final JavaPlugin plugin;
    private final File alertFile;

    @Getter
    private final Set<UUID> alertEnabled = ConcurrentHashMap.newKeySet();

    @Inject
    public AlertManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.alertFile = new File(plugin.getDataFolder(), "alerts.yml");
        loadAlerts();
    }

    private void loadAlerts() {
        if (!alertFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(alertFile);
        List<String> uuids = config.getStringList("enabled-players");

        for (String uuidStr : uuids) {
            try {
                alertEnabled.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in alerts.yml: " + uuidStr);
            }
        }

        plugin.getLogger().info("Loaded " + alertEnabled.size() + " players with alerts enabled");
    }

    private void saveAlerts() {
        YamlConfiguration config = new YamlConfiguration();

        List<String> uuids = alertEnabled.stream()
            .map(UUID::toString)
            .toList();

        config.set("enabled-players", uuids);

        try {
            config.save(alertFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save alerts.yml: " + e.getMessage());
        }
    }

    public void enableAlerts(Player player) {
        alertEnabled.add(player.getUniqueId());
        saveAlerts();
    }

    public void disableAlerts(Player player) {
        alertEnabled.remove(player.getUniqueId());
        saveAlerts();
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
