package pw.eisphoenix.aquacore.permission;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.AquaCore;
import pw.eisphoenix.aquacore.CPlayer;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.PermissionService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "rank",
        description = "Manages the rank",
        usage = "/%CMD% <player> [rankname]",
        permission = "core.rank"
)
public final class RankCommand extends CCommand {
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private PermissionService permissionService;
    @Inject
    private MessageService messageService;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }

        final CPlayer cPlayer = playerInfoService.getPlayer(args[0]);

        if (cPlayer == null) {
            sender.sendMessage(messageService.getMessage("cmd.error.player", MessageService.MessageType.WARNING)
                    .replaceAll("%NAME%", args[0]));
            return;
        }

        if (args.length == 1) {
            if (sender instanceof Player) {
                getCommandSystem().sudoAction((Player) sender, player -> {
                    player.sendMessage(
                            messageService.getMessage("rank.info", MessageService.MessageType.INFO)
                                    .replaceAll("%NAME%", cPlayer.getActualUsername())
                                    .replaceAll("%RANK%", cPlayer.getRank().getDisplayName())
                    );
                }, 30);
            } else {
                sender.sendMessage(
                        messageService.getMessage("rank.info", MessageService.MessageType.INFO)
                                .replaceAll("%NAME%", cPlayer.getActualUsername())
                                .replaceAll("%RANK%", cPlayer.getRank().getDisplayName())
                );
            }
            return;
        }

        if (args.length == 2) {
            final Rank rank = permissionService.getRank(args[1].toLowerCase());
            if (rank == null) {
                sender.sendMessage(
                        messageService.getMessage("rank.error", MessageService.MessageType.WARNING)
                                .replaceAll("%RANK%", args[1])
                );
                return;
            }
            if (!sender.hasPermission("rank.set." + rank.getName())) {
                sender.sendMessage(
                        messageService.getMessage("rank.error.noperm", MessageService.MessageType.ERROR)
                );
                return;
            }
            final Player target = Bukkit.getPlayer(cPlayer.getUuid());
            if (sender instanceof Player) {
                getCommandSystem().sudoAction((Player) sender, player -> {
                    if (!player.hasPermission("rank.set." + rank.getName())) {
                        player.sendMessage(
                                messageService.getMessage("rank.error.noperm", MessageService.MessageType.ERROR)
                        );
                        return;
                    }
                    cPlayer.setRank(rank);
                    playerInfoService.savePlayer(cPlayer);
                    if (target != null) {
                        AquaCore.getInstance().getPermissionSystem().updatePermissions(target);
                    }
                    player.sendMessage(
                            messageService.getMessage("rank.success", MessageService.MessageType.INFO)
                                    .replaceAll("%NAME%", cPlayer.getActualUsername())
                                    .replaceAll("%RANK%", cPlayer.getRank().getDisplayName())
                    );
                }, 30);
            } else {
                cPlayer.setRank(rank);
                playerInfoService.savePlayer(cPlayer);
                if (target != null) {
                    AquaCore.getInstance().getPermissionSystem().updatePermissions(target);
                }
                sender.sendMessage(
                        messageService.getMessage("rank.success", MessageService.MessageType.INFO)
                                .replaceAll("%NAME%", cPlayer.getActualUsername())
                                .replaceAll("%RANK%", cPlayer.getRank().getDisplayName())
                );
            }
        }
    }

    public List<String> onTabComplete(final CommandSender sender, final String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1) {
            return super.onTabComplete(sender, args);
        }
        if (args.length == 2) {
            return permissionService.getRanks().stream()
                    .map(Rank::getName).filter(name -> name.startsWith(args[1])).collect(Collectors.toList());
        }
        return ImmutableList.of();
    }
}
