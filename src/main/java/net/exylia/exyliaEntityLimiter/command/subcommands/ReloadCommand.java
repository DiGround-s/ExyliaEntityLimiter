package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.config.ConfigManager;
import net.exylia.exyliaEntityLimiter.task.TaskScheduler;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.concurrent.CompletableFuture;

@Singleton
public class ReloadCommand {
    private final ConfigManager configManager;
    private final TaskScheduler taskScheduler;

    @Inject
    public ReloadCommand(ConfigManager configManager, TaskScheduler taskScheduler) {
        this.configManager = configManager;
        this.taskScheduler = taskScheduler;
    }

    @Command("entitylimiter reload")
    @CommandPermission("entitylimiter.reload")
    public void reload(CommandSender sender) {
        MessageUtil.send(sender, "<gradient:#8a51c4:#aa76de>Reloading configuration...</gradient>");

        CompletableFuture.runAsync(() -> {
            configManager.reloadConfig();
            taskScheduler.restart();
        }).thenRun(() -> {
            sender.sendMessage(MessageUtil.success("Configuration reloaded successfully!"));
            MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Removal Strategy:</#59a4ff> <#e7cfff>" +
                configManager.getConfig().getRemovalStrategy() + "</#e7cfff>");
            MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Cleanup Interval:</#59a4ff> <#e7cfff>" +
                configManager.getConfig().getCleanupInterval() + " ticks</#e7cfff>");
        });
    }
}
