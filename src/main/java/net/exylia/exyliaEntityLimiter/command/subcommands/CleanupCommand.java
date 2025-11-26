package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Singleton
public class CleanupCommand {
    private final EntityLimiterService service;

    @Inject
    public CleanupCommand(EntityLimiterService service) {
        this.service = service;
    }

    @Command("entitylimiter cleanup")
    @CommandPermission("entitylimiter.cleanup")
    public void cleanup(Player player) {
        Chunk chunk = player.getLocation().getChunk();

        MessageUtil.send(player, "<gradient:#8a51c4:#aa76de>Processing chunk cleanup...</gradient>");

        service.cleanupChunk(chunk).thenAccept(removed -> {
            if (removed > 0) {
                player.sendMessage(MessageUtil.success("Removed " + removed + " entities"));
                MessageUtil.send(player, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Chunk:</#59a4ff> " +
                    MessageUtil.clickableChunk(chunk, "teleport"));
            } else {
                player.sendMessage(MessageUtil.info("No entities exceeded limits in this chunk"));
            }
        });
    }
}
