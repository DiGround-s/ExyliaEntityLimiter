package net.exylia.exyliaEntityLimiter.util;

import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Singleton
public class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final String PRIMARY = "#8a51c4";
    private static final String SECONDARY = "#aa76de";
    private static final String SECONDARY_LIGHT = "#b48fd9";
    private static final String TEXT_NORMAL = "#e7cfff";
    private static final String TEXT_DARK = "#b9a5cc";
    private static final String TEXT_VERY_DARK = "#a89ab5";
    private static final String ERROR = "#a33b53";
    private static final String ERROR_LIGHT = "#b36476";
    private static final String SUCCESS = "#8fffc1";
    private static final String SUCCESS_LIGHT = "#a1ffc3";
    private static final String WARNING = "#ffc58f";
    private static final String WARNING_LIGHT = "#ffd2a8";
    private static final String INFO = "#59a4ff";
    private static final String INFO_LIGHT = "#7db7ff";

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
    }

    public static Component header(String title) {
        return parse("<gradient:" + PRIMARY + ":" + SECONDARY + "><st>                                        </st></gradient>\n" +
                "<gradient:" + PRIMARY + ":" + SECONDARY + "><bold>║</bold></gradient> <gradient:" + TEXT_NORMAL + ":" + SECONDARY_LIGHT + "><bold>" + title + "</bold></gradient> <gradient:" + PRIMARY + ":" + SECONDARY + "><bold>║</bold></gradient>\n");
    }

    public static Component footer() {
        return parse("<gradient:" + PRIMARY + ":" + SECONDARY + "><st>                                        </st></gradient>");
    }

    public static Component error(String message) {
        return parse("<gradient:" + ERROR + ":" + ERROR_LIGHT + ">✖</gradient> <" + ERROR_LIGHT + ">" + message + "</" + ERROR_LIGHT + ">");
    }

    public static Component success(String message) {
        return parse("<gradient:" + SUCCESS + ":" + SUCCESS_LIGHT + ">✔</gradient> <" + SUCCESS + ">" + message + "</" + SUCCESS + ">");
    }

    public static Component info(String message) {
        return parse("<gradient:" + INFO + ":" + INFO_LIGHT + ">ⓘ</gradient> <" + INFO_LIGHT + ">" + message + "</" + INFO_LIGHT + ">");
    }

    public static Component warning(String message) {
        return parse("<gradient:" + WARNING + ":" + WARNING_LIGHT + ">⚠</gradient> <" + WARNING + ">" + message + "</" + WARNING + ">");
    }

    public static Component clickableChunk(Chunk chunk, String action) {
        String coords = chunk.getX() + ", " + chunk.getZ();
        String command = "/entitylimiter teleport " + chunk.getWorld().getName() + " " + chunk.getX() + " " + chunk.getZ();

        return parse("<hover:show_text:'<gradient:" + SUCCESS + ":" + SUCCESS_LIGHT + ">Click to teleport</gradient>'>" +
                "<click:run_command:'" + command + "'>" +
                "<gradient:" + SECONDARY + ":" + SECONDARY_LIGHT + ">[" + coords + "]</gradient>" +
                "</click></hover>");
    }

    public static Component statLine(String label, String value, String color) {
        return parse("  <" + TEXT_DARK + ">▸</" + TEXT_DARK + "> <" + color + ">" + label + ": <" + TEXT_NORMAL + "><bold>" + value + "</bold></" + TEXT_NORMAL + ">");
    }

    public static Component percentage(double value) {
        String color;
        if (value >= 80) {
            color = SUCCESS;
        } else if (value >= 50) {
            color = WARNING;
        } else {
            color = ERROR;
        }
        return parse("<gradient:" + color + ":" + color + ">" + String.format("%.1f%%", value) + "</gradient>");
    }

    public static Component progressBar(int current, int max, int length) {
        double percentage = max > 0 ? (double) current / max : 0;
        int filled = (int) (percentage * length);
        int empty = length - filled;

        String color;
        if (percentage >= 1.0) {
            color = ERROR;
        } else if (percentage >= 0.8) {
            color = WARNING;
        } else {
            color = SUCCESS;
        }

        StringBuilder bar = new StringBuilder("<gradient:" + color + ":" + TEXT_NORMAL + ">[");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("</gradient>");
        for (int i = 0; i < empty; i++) {
            bar.append("<" + TEXT_VERY_DARK + ">█</" + TEXT_VERY_DARK + ">");
        }
        bar.append(" <" + TEXT_NORMAL + ">").append(current).append("/").append(max).append("</" + TEXT_NORMAL + ">");

        return parse(bar.toString());
    }

    public static Component pagination(int currentPage, int totalPages, String baseCommand) {
        Component result = Component.empty();

        if (currentPage > 1) {
            result = result.append(parse("<hover:show_text:'<" + SUCCESS + ">Previous page</" + SUCCESS + ">'>" +
                    "<click:run_command:'" + baseCommand + " " + (currentPage - 1) + "'>" +
                    "<gradient:" + PRIMARY + ":" + SECONDARY + ">◄ Prev</gradient>" +
                    "</click></hover> "));
        } else {
            result = result.append(parse("<" + TEXT_VERY_DARK + ">◄ Prev</" + TEXT_VERY_DARK + "> "));
        }

        result = result.append(parse("<gradient:" + PRIMARY + ":" + SECONDARY + ">Page " + currentPage + "/" + totalPages + "</gradient> "));

        if (currentPage < totalPages) {
            result = result.append(parse("<hover:show_text:'<" + SUCCESS + ">Next page</" + SUCCESS + ">'>" +
                    "<click:run_command:'" + baseCommand + " " + (currentPage + 1) + "'>" +
                    "<gradient:" + PRIMARY + ":" + SECONDARY + ">Next ►</gradient>" +
                    "</click></hover>"));
        } else {
            result = result.append(parse("<" + TEXT_VERY_DARK + ">Next ►</" + TEXT_VERY_DARK + ">"));
        }

        return result;
    }

    public static Component button(String label, String command, String hover) {
        return parse("<hover:show_text:'" + hover + "'>" +
                "<click:run_command:'" + command + "'>" +
                "<gradient:" + PRIMARY + ":" + SECONDARY + ">[" + label + "]</gradient>" +
                "</click></hover>");
    }
}
