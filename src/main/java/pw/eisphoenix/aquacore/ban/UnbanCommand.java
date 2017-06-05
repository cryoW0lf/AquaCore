package pw.eisphoenix.aquacore.ban;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.service.BanService;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

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

        playerInfoService.getPlayer(args[0]).thenAccept(cPlayer -> {
            if (cPlayer == null) {
                messageService.getMessage("ban.error.player", MessageService.MessageType.WARNING).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", args[0]))
                );
                return;
            }
            final BanEntry banEntry = banService.getActualBan(cPlayer).join();

            if (banEntry == null) {
                messageService.getMessage("unban.error.noban", MessageService.MessageType.WARNING).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", cPlayer.getActualUsername()))
                );
                return;
            }

            if (!sender.hasPermission("core.unban." + banEntry.getBanReason().getName())) {
                messageService.getMessage("unban.error.perm", MessageService.MessageType.ERROR).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%REASON%", banEntry.getBanReason().getDisplayName()))
                );
                return;
            }

            if (banEntry.getUntil() == -1 && !sender.hasPermission("core.unban.permanent")) {
                messageService.getMessage("unban.error.permanent", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                return;
            }
            final boolean result = banService.unbanPlayer(banEntry,
                    sender instanceof Player ? ((Player) sender).getUniqueId() : SYSTEM_UUID).join();
            messageService.getMessage(result ? "unban.success" : "unban.error",
                    result ? MessageService.MessageType.INFO : MessageService.MessageType.ERROR).thenAccept(
                    message -> sender.sendMessage(message.replaceAll("%NAME%", cPlayer.getActualUsername()))
            );
        });
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
        messageService.getMessage("ban.format").thenAccept(message -> dateFormat = new SimpleDateFormat(message));
    }
}
