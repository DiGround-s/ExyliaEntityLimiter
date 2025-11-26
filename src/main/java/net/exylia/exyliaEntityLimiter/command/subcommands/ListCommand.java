package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.core.ChunkEntityData;
import net.exylia.exyliaEntityLimiter.core.ChunkEntityTracker;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ListCommand {
    private final EntityLimiterService service;
    private final ChunkEntityTracker tracker;

    @Inject
    public ListCommand(EntityLimiterService service, ChunkEntityTracker tracker) {
        this.service = service;
        this.tracker = tracker;
    }

    @Command("entitylimiter list")
    @CommandPermission("entitylimiter.list")
    public void list(Player player, @Default("1") int page) {
        MessageUtil.send(player, "<gradient:#8a51c4:#aa76de>Analyzing loaded chunks...</gradient>");

        CompletableFuture.supplyAsync(() -> {
            List<ChunkEntityData> chunkDataList = new ArrayList<>();

            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    Map<EntityType, Integer> counts = tracker.getEntityCounts(chunk);
                    int total = counts.values().stream().mapToInt(Integer::intValue).sum();

                    ChunkEntityTracker.LimitCheckResult result = tracker.checkLimits(chunk);
                    int excess = result.getTotalExcess();

                    chunkDataList.add(new ChunkEntityData(
                            chunk,
                            counts,
                            total,
                            excess,
                            result.hasExceeded()
                    ));
                }
            }

            Collections.sort(chunkDataList);
            return chunkDataList;
        }).thenAccept(chunks -> {
            displayChunkList(player, chunks, page);
        });
    }

    private void displayChunkList(Player player, List<ChunkEntityData> chunks, int page) {
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) chunks.size() / itemsPerPage);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, chunks.size());

        player.sendMessage(MessageUtil.header("Chunk List - Priority Order"));
        MessageUtil.send(player, "  <#b9a5cc>Loaded Chunks:</#b9a5cc> <#e7cfff><bold>" + chunks.size() + "</bold></#e7cfff>");
        MessageUtil.send(player, "  <#b9a5cc>Showing:</#b9a5cc> <#59a4ff>" + (startIndex + 1) + "-" + endIndex + "</#59a4ff>");
        player.sendMessage(Component.empty());

        for (int i = startIndex; i < endIndex; i++) {
            ChunkEntityData data = chunks.get(i);

            String status;
            if (data.isHasExceeded()) {
                status = "<gradient:#a33b53:#b36476>⚠ EXCEEDED</gradient>";
            } else if (data.getTotalEntities() > 50) {
                status = "<gradient:#ffc58f:#ffd2a8>⚡ HIGH</gradient>";
            } else {
                status = "<gradient:#8fffc1:#a1ffc3>✓ NORMAL</gradient>";
            }

            Component rankComponent = MessageUtil.parse("  <gradient:#8a51c4:#b48fd9><bold>#" + (i + 1) + "</bold></gradient> ");
            Component statusComponent = MessageUtil.parse(status + " ");
            Component chunkComponent = MessageUtil.clickableChunk(data.getChunk(), "teleport");

            Component line = rankComponent
                    .append(statusComponent)
                    .append(chunkComponent)
                    .append(MessageUtil.parse(" <#b9a5cc>→</#b9a5cc> "));

            if (data.isHasExceeded()) {
                line = line.append(MessageUtil.parse("<#a33b53>" + data.getTotalEntities() + " entities</#a33b53> " +
                        "<#b36476>(+" + data.getExcessEntities() + " excess)</#b36476>"));
            } else {
                line = line.append(MessageUtil.parse("<#e7cfff>" + data.getTotalEntities() + " entities</#e7cfff>"));
            }

            Component cleanupButton = MessageUtil.button(
                    "Teleport",
                    "/entitylimiter teleport " + data.getWorldName() + " " + data.getX() + " " + data.getZ(),
                    "<gradient:#59a4ff:#7db7ff>Click to teleport</gradient>"
            );

            player.sendMessage(line.append(Component.text(" ")).append(cleanupButton));
        }

        player.sendMessage(Component.empty());
        player.sendMessage(MessageUtil.pagination(page, totalPages, "/entitylimiter list"));
        player.sendMessage(MessageUtil.footer());
    }
}
