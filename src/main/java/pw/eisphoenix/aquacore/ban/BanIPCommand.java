package pw.eisphoenix.aquacore.ban;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.CPlayer;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "banip",
        description = "Bans a IP",
        usage = "/%CMD% <ip> <reason>",
        permission = "core.ban.ip"
)
public final class BanIPCommand extends CCommand implements InjectionHook {
    private final static UUID SYSTEM_UUID = UUID.nameUUIDFromBytes("SYSTEM".getBytes());
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private BanService banService;
    @Inject
    private MessageService messageService;
    private SimpleDateFormat dateFormat;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }

        final BanReason banReason = banService.getBanReason(args[1]);

        if (banReason == null) {
            sender.sendMessage(messageService.getMessage("ban.error.reason", MessageService.MessageType.WARNING)
                    .replaceAll("%REASON%", args[1].toUpperCase()));
            return;
        }

        if (!sender.hasPermission("core.ban." + banReason.getName())) {
            sender.sendMessage(messageService.getMessage("ban.error.perm", MessageService.MessageType.ERROR)
                    .replaceAll("%REASON%", banReason.getDisplayName()));
        }

        final Player player = Bukkit.getPlayer(args[0]);
        InetAddress inetAddress = null;
        try {
            if (player == null) {
                inetAddress = InetAddress.getByName(args[0]);
            } else {
                inetAddress = player.getAddress().getAddress();
            }
        } catch (final UnknownHostException ignored) {
        }

        if (inetAddress == null) {
            sender.sendMessage(
                    messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR)
            );
            return;
        }

        final CPlayer cPlayer = player == null ? null : playerInfoService.getPlayer(player);

        final BanEntry banEntry;
        if (sender instanceof Player) {
            banEntry = banService.banIP(inetAddress, ((Player) sender).getUniqueId(), banReason);
            if (cPlayer != null) {
                banService.banPlayer(cPlayer, ((Player) sender).getUniqueId(), banReason);
            }
        } else if (sender instanceof ConsoleCommandSender) {
            banEntry = banService.banIP(inetAddress, SYSTEM_UUID, banReason);
            if (cPlayer != null) {
                banService.banPlayer(cPlayer, SYSTEM_UUID, banReason);
            }
        } else {
            sender.sendMessage(messageService.getMessage("cmd.error.sender", MessageService.MessageType.ERROR));
            return;
        }
        if (banEntry == null) {
            sender.sendMessage(messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR));
            return;
        }
        sender.sendMessage(messageService.getMessage("ban.success.ip", MessageService.MessageType.INFO)
                .replaceAll("%IP%", inetAddress.getHostAddress())
                .replaceAll("%REASON%", banReason.getDisplayName())
                .replaceAll("%DATE%", dateFormat.format(new Date(banEntry.getUntil()))));
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1) {
            return super.onTabComplete(sender, args);
        } else if (args.length == 2) {
            return banService.getBanReasons(args[1]);
        }
        return ImmutableList.of();
    }

    @Override
    public void postInjection() {
        dateFormat = new SimpleDateFormat(messageService.getMessage("ban.format"));
    }
}
