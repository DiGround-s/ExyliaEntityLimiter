package net.exylia.exyliaEntityLimiter.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaEntityLimiter.alert.AlertManager;
import net.exylia.exyliaEntityLimiter.util.MessageUtil;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Singleton
public class AlertCommand {
    private final AlertManager alertManager;

    @Inject
    public AlertCommand(AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    @Command("entitylimiter alert")
    @CommandPermission("entitylimiter.alert")
    public void alert(Player player) {
        alertManager.toggleAlerts(player);

        if (alertManager.hasAlertsEnabled(player)) {
            player.sendMessage(MessageUtil.success("Entity removal alerts enabled"));
            MessageUtil.send(player, "  <#b9a5cc>▸</#b9a5cc> <#e7cfff>You will receive notifications when entities are removed</#e7cfff>");
        } else {
            player.sendMessage(MessageUtil.info("Entity removal alerts disabled"));
        }
    }
}
