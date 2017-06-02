package pw.eisphoenix.aquacore.ban;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

import java.util.List;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "kick",
        description = "Kicks a Player",
        usage = "/%CMD% <player> <reason>",
        permission = "core.kick"
)
public final class KickCommand extends CCommand {
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private MessageService messageService;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }

        final Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(
                    messageService.getMessage("ban.error.player", MessageService.MessageType.WARNING)
                            .replaceAll("%PLAYER%", args[0])
            );
            return;
        }
        if (!sender.hasPermission("core.ban.exempt") && player.hasPermission("core.kick.exempt")) {
            sender.sendMessage(
                    messageService.getMessage("kick.exempt", MessageService.MessageType.ERROR)
            );
            return;
        }
        StringBuilder reason = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]);
        }
        player.kickPlayer(
                messageService.getMessage("kick.message")
                        .replaceAll("%REASON", reason.toString())
                        .replaceAll("%NAME%", sender.getName())
        );
        sender.sendMessage(
                messageService.getMessage("kick.success", MessageService.MessageType.INFO)
                        .replaceAll("%NAME", player.getName())
                        .replaceAll("%REASON%", reason.toString())
        );
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1) {
            return super.onTabComplete(sender, args);
        }
        return ImmutableList.of();
    }
}
