package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.core.EntityLimiterService;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Singleton
public class StatsCommand {
    private final EntityLimiterService service;

    @Inject
    public StatsCommand(EntityLimiterService service) {
        this.service = service;
    }

    @Command("entitylimiter stats")
    @CommandPermission("entitylimiter.stats")
    public void stats(CommandSender sender) {
        CacheStats chunkStats = service.getChunkCacheStats();
        CacheStats bypassStats = service.getBypassCacheStats();

        sender.sendMessage(MessageUtil.header("Cache Statistics"));
        sender.sendMessage(Component.empty());

        MessageUtil.send(sender, "<gradient:#8a51c4:#b48fd9><bold>CHUNK CACHE</bold></gradient>");
        sender.sendMessage(MessageUtil.statLine("Hit Rate", "", "#8fffc1")
                .append(MessageUtil.percentage(chunkStats.hitRate() * 100)));
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#8fffc1>Hits:</#8fffc1> <#e7cfff><bold>" +
                formatNumber(chunkStats.hitCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#a33b53>Misses:</#a33b53> <#e7cfff><bold>" +
                formatNumber(chunkStats.missCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Load Count:</#59a4ff> <#e7cfff><bold>" +
                formatNumber(chunkStats.loadCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#ffc58f>Evictions:</#ffc58f> <#e7cfff><bold>" +
                formatNumber(chunkStats.evictionCount()) + "</bold></#e7cfff>");

        sender.sendMessage(Component.empty());

        MessageUtil.send(sender, "<gradient:#aa76de:#b48fd9><bold>BYPASS CACHE</bold></gradient>");
        sender.sendMessage(MessageUtil.statLine("Hit Rate", "", "#8fffc1")
                .append(MessageUtil.percentage(bypassStats.hitRate() * 100)));
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#8fffc1>Hits:</#8fffc1> <#e7cfff><bold>" +
                formatNumber(bypassStats.hitCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#a33b53>Misses:</#a33b53> <#e7cfff><bold>" +
                formatNumber(bypassStats.missCount()) + "</bold></#e7cfff>");

        sender.sendMessage(Component.empty());
        sender.sendMessage(MessageUtil.footer());
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
}
