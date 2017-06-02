package pw.eisphoenix.aquacore.ban;

import com.google.common.collect.ImmutableList;
import pw.eisphoenix.aquacore.CPlayer;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.service.BanService;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "unban",
        description = "Unbans a Player",
        usage = "/%CMD% <player>",
        permission = "core.unban"
)
public final class UnbanCommand extends CCommand implements InjectionHook {
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

        CPlayer cPlayer = playerInfoService.getPlayer(args[0]);

        if (cPlayer == null) {
            sender.sendMessage(messageService.getMessage("ban.error.player", MessageService.MessageType.WARNING)
                    .replaceAll("%NAME%", args[0]));
            return;
        }

        final BanEntry banEntry = banService.getActualBan(cPlayer);

        if (banEntry == null) {
            sender.sendMessage(messageService.getMessage("unban.error.noban", MessageService.MessageType.WARNING)
                    .replaceAll("%PLAYER%", cPlayer.getActualUsername()));
            return;
        }

        if (!sender.hasPermission("core.unban." + banEntry.getBanReason().getName())) {
            sender.sendMessage(messageService.getMessage("unban.error.perm", MessageService.MessageType.ERROR)
                    .replaceAll("%REASON%", banEntry.getBanReason().getDisplayName()));
        }

        if (banEntry.getUntil() == -1 && !sender.hasPermission("core.unban.permanent")) {
            sender.sendMessage(messageService.getMessage("unban.error.permanent",
                    MessageService.MessageType.ERROR));
            return;
        }

        final boolean unbanned;

        if (sender instanceof Player) {
            unbanned = banService.unbanPlayer(banEntry, ((Player) sender).getUniqueId());
        } else if (sender instanceof ConsoleCommandSender) {
            unbanned = banService.unbanPlayer(banEntry, SYSTEM_UUID);
        } else {
            sender.sendMessage(messageService.getMessage("cmd.error.sender", MessageService.MessageType.ERROR));
            return;
        }
        sender.sendMessage(messageService.getMessage(unbanned ? "unban.success" : "unban.error",
                unbanned ? MessageService.MessageType.INFO : MessageService.MessageType.WARNING)
                .replaceAll("%NAME%", cPlayer.getActualUsername()));
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
