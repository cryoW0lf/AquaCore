package pw.eisphoenix.aquacore.misc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "ping",
        description = "Shows the ping",
        permission = "core.ping"
)
public final class PingCommand extends CCommand {
    private final NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
    @Inject
    private MessageService messageService;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                messageService.getMessage("cmd.sender.console", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                return;
            }
            final Player player = (Player) sender;
            messageService.getMessage("ping.info", MessageService.MessageType.INFO).thenAccept(
                    message -> sender.sendMessage(message.replaceAll("%PING%", String.valueOf(player.spigot().getPing())))
            );
            return;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("core.ping.others")) {
                messageService.getMessage("cmd.error.perm", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                return;
            }
            final Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                messageService.getMessage("cmd.error.player", MessageService.MessageType.ERROR).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", args[0]))
                );
                return;
            }
            messageService.getMessage("ping.info.others", MessageService.MessageType.INFO).thenAccept(
                    message -> sender.sendMessage(message
                            .replaceAll("%NAME%", player.getName())
                            .replaceAll("%PING%", String.valueOf(player.spigot().getPing()))
                    )
            );
        }
    }

    private String formatTPS(final double tps) {
        return "§" + (tps > 19 ? "a" : tps > 15 ? "e" : "c") + format.format(tps);
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1 && sender.hasPermission("core.ping.others")) {
            return super.onTabComplete(sender, args);
        }

        return ImmutableList.of();
    }
}
