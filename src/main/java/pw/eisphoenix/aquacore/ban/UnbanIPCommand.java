package pw.eisphoenix.aquacore.ban;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.service.BanService;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "unbanip",
        description = "Unbans a IP",
        usage = "/%CMD% <ip>",
        permission = "core.unban.ip"
)
public final class UnbanIPCommand extends CCommand implements InjectionHook {
    private final static UUID SYSTEM_UUID = UUID.nameUUIDFromBytes("SYSTEM".getBytes());
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private BanService banService;
    @Inject
    private MessageService messageService;
    private SimpleDateFormat dateFormat;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(args[0]);
        } catch (final UnknownHostException ignored) {
        }

        if (inetAddress == null) {
            sender.sendMessage(
                    messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR)
            );
            return;
        }

        final BanEntry banEntry = banService.getActualBan(inetAddress);

        if (banEntry == null) {
            sender.sendMessage(messageService.getMessage("unban.error.noban.ip", MessageService.MessageType.WARNING)
                    .replaceAll("%IP%", inetAddress.getHostAddress())
            );
            return;
        }

        if (!sender.hasPermission("core.unban." + banEntry.getBanReason().getName())) {
            sender.sendMessage(
                    messageService.getMessage("unban.error.perm", MessageService.MessageType.ERROR)
                            .replaceAll("%REASON%", banEntry.getBanReason().getDisplayName())
            );
        }

        if (banEntry.getUntil() == -1 && !sender.hasPermission("core.unban.permanent")) {
            sender.sendMessage(
                    messageService.getMessage("unban.error.permanent", MessageService.MessageType.ERROR)
            );
            return;
        }

        final boolean unbanned;

        if (sender instanceof Player) {
            unbanned = banService.unbanPlayer(banEntry, ((Player) sender).getUniqueId());
        } else if (sender instanceof ConsoleCommandSender) {
            unbanned = banService.unbanPlayer(banEntry, SYSTEM_UUID);
        } else {
            sender.sendMessage(
                    messageService.getMessage("cmd.error.sender", MessageService.MessageType.ERROR)
            );
            return;
        }
        sender.sendMessage(
                messageService.getMessage(unbanned ? "unban.success.ip" : "unban.error",
                        unbanned ? MessageService.MessageType.INFO : MessageService.MessageType.WARNING)
                        .replaceAll("%IP%", inetAddress.getHostAddress())
        );
    }

    public List<String> onTabComplete(final CommandSender sender, final String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1) {
            return super.onTabComplete(sender, args);
        }
        return ImmutableList.of();
    }

    @Override
    public void postInjection() {
        dateFormat = new SimpleDateFormat(messageService.getMessage("ban.format"));
    }
}