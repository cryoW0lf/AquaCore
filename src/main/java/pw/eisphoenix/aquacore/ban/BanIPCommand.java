package pw.eisphoenix.aquacore.ban;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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

        banService.getBanReason(args[1]).thenAccept(banReason -> {
            if (banReason == null) {
                messageService.getMessage("ban.error.reason", MessageService.MessageType.WARNING).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%REASON%", args[1].toUpperCase()))
                );
                return;
            }
            if (!sender.hasPermission("core.ban." + banReason.getName())) {
                messageService.getMessage("ban.error.perm", MessageService.MessageType.ERROR).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", banReason.getDisplayName()))
                );
                return;
            }

            final Player player = Bukkit.getPlayer(args[0]);
            InetAddress inetAddress = null;
            try {
                inetAddress = player == null ? InetAddress.getByName(args[0]) : player.getAddress().getAddress();
            } catch (Exception ignored) {
            }
            if (inetAddress == null) {
                messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                return;
            }

            final CPlayer cPlayer = player == null ? null : playerInfoService.getPlayer(player).join();
            final UUID source = sender instanceof Player ? ((Player) sender).getUniqueId() : SYSTEM_UUID;
            final BanEntry banEntry = banService.banIP(inetAddress, source, banReason).join();
            if (cPlayer != null) {
                banService.banPlayer(cPlayer, source, banReason);
            }

            if (banEntry == null) {
                messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                return;
            }
            final InetAddress finalInetAddress = inetAddress;
            messageService.getMessage("ban.success", MessageService.MessageType.INFO).thenAccept(
                    message -> sender.sendMessage(message
                            .replaceAll("%IP%", finalInetAddress.getHostAddress())
                            .replaceAll("%REASON%", banReason.getDisplayName())
                            .replaceAll("%DATE%", dateFormat.format(new Date(banEntry.getUntil())))
                    )
            );
        });
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
        messageService.getMessage("ban.format").thenAccept(message -> dateFormat = new SimpleDateFormat(message));
    }
}
