package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Map;

@Singleton
public class InfoCommand {
    private final EntityLimiterService service;

    @Inject
    public InfoCommand(EntityLimiterService service) {
        this.service = service;
    }

    @Command("entitylimiter info")
    @CommandPermission("entitylimiter.info")
    public void info(Player player) {
        Chunk chunk = player.getLocation().getChunk();

        service.getEntityCount(chunk).thenAccept(counts -> {
            player.sendMessage(MessageUtil.header("Chunk Entity Info"));
            MessageUtil.send(player, "  <#59a4ff>Location:</#59a4ff> ");
            player.sendMessage(MessageUtil.clickableChunk(chunk, "teleport"));
            MessageUtil.send(player, "  <#59a4ff>World:</#59a4ff> <#e7cfff>" + chunk.getWorld().getName() + "</#e7cfff>");
            player.sendMessage(Component.empty());

            if (counts.isEmpty()) {
                player.sendMessage(MessageUtil.info("No entities in this chunk"));
                player.sendMessage(MessageUtil.footer());
                return;
            }

            int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            MessageUtil.send(player, "  <gradient:#8a51c4:#b48fd9>Total Entities:</gradient> <#e7cfff><bold>" + total + "</bold></#e7cfff>");
            player.sendMessage(Component.empty());

            counts.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(15)
                    .forEach(entry -> {
                        EntityType type = entry.getKey();
                        int count = entry.getValue();
                        int limit = service.getLimit(type);

                        String typeStr = formatEntityType(type);

                        if (limit >= 0) {
                            Component bar = MessageUtil.progressBar(count, limit, 15);
                            Component line = MessageUtil.parse("  <#b9a5cc>▸</#b9a5cc> <#e7cfff>" + typeStr + ":</#e7cfff> ");
                            player.sendMessage(line.append(bar));
                        } else {
                            MessageUtil.send(player, "  <#b9a5cc>▸</#b9a5cc> <#e7cfff>" + typeStr + ":</#e7cfff> " +
                                    "<gradient:#8fffc1:#a1ffc3>" + count + "</gradient> <#a89ab5>(no limit)</#a89ab5>");
                        }
                    });

            player.sendMessage(Component.empty());
            player.sendMessage(MessageUtil.footer());
        });
    }

    private String formatEntityType(EntityType type) {
        String name = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
