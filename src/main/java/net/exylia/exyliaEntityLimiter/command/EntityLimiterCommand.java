package net.exylia.exyliaEntityLimiter.command;

import com.google.inject.Injector;
import net.exylia.exyliaEntityLimiter.ExyliaEntityLimiter;
import net.exylia.exyliaEntityLimiter.command.subcommands.*;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class EntityLimiterCommand {
    private final ExyliaEntityLimiter plugin;
    private final Lamp<BukkitCommandActor> lamp;

    public EntityLimiterCommand(ExyliaEntityLimiter plugin, Injector injector) {
        this.plugin = plugin;
        this.lamp = BukkitLamp.builder(plugin).build();
        registerCommands(injector);
    }

    private void registerCommands(Injector injector) {
        lamp.register(injector.getInstance(ReloadCommand.class));
        lamp.register(injector.getInstance(StatsCommand.class));
        lamp.register(injector.getInstance(CleanupCommand.class));
        lamp.register(injector.getInstance(InfoCommand.class));
        lamp.register(injector.getInstance(ListCommand.class));
        lamp.register(injector.getInstance(TeleportCommand.class));
        lamp.register(injector.getInstance(AlertCommand.class));
    }
}
