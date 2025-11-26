package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Singleton
public class TeleportCommand {

    @Inject
    public TeleportCommand() {
    }

    @Command("entitylimiter teleport")
    @CommandPermission("entitylimiter.teleport")
    public void teleport(Player player, String worldName, int chunkX, int chunkZ) {
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(MessageUtil.error("World '" + worldName + "' not found"));
            return;
        }

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int y = world.getHighestBlockYAt(blockX, blockZ) + 1;

        Location location = new Location(world, blockX, y, blockZ);

        player.teleport(location);
        player.sendMessage(MessageUtil.success("Teleported to chunk [" + chunkX + ", " + chunkZ + "]"));
        MessageUtil.send(player, "  <gray>▸</gray> <aqua>World:</aqua> <yellow>" + worldName + "</yellow>");
        MessageUtil.send(player, "  <gray>▸</gray> <aqua>Position:</aqua> <yellow>" + blockX + ", " + y + ", " + blockZ + "</yellow>");
    }
}
