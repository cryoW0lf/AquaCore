package pw.eisphoenix.aquacore.totp;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.cmd.CommandOption;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;
import pw.eisphoenix.aquacore.service.SecurityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "totp",
        description = "Command for TimeBaseOneTimePasswords",
        usage = "/%CMD% <create/exit/code/reset> [player]",
        permission = "totp.use",
        options = @CommandOption(forConsole = false),
        aliase = {"code"}
)
public final class TOTPCommand extends CCommand {
    private List<String> subCommands = ImmutableList.of("create", "exit", "<code>", "reset");
    @Inject
    private MessageService messageService;
    @Inject
    private SecurityService securityService;
    @Inject
    private PlayerInfoService playerInfoService;
    private Map<UUID, Long> timeStamps = new HashMap<>();
    private Map<UUID, SudoAction> actions = new HashMap<>();

    public void onCommand(final CommandSender sender, final String[] args) {
        Validate.notNull(sender);
        Validate.notNull(args);
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }
        final UUID uuid = ((Player) sender).getUniqueId();

        switch (args[0].toLowerCase()) {
            case "create":
                securityService.hasKey(uuid).thenAccept(hasKey -> {
                    if (hasKey) {
                        messageService.getMessage("totp.alreadyexists", MessageService.MessageType.WARNING).thenAccept(sender::sendMessage);
                        messageService.getMessage("cmd.confirm", MessageService.MessageType.WARNING).thenAccept(
                                message -> sender.sendMessage(message
                                        .replaceAll("%MESSAGE%", "/totp confirm")
                                        .replaceAll("%TIME%", "10 Sekunden")
                                )
                        );
                        timeStamps.put(uuid, System.currentTimeMillis() + 100000);
                        return;
                    }
                    messageService.getMessage("totp.create", MessageService.MessageType.INFO).thenAccept(
                            message -> sender.sendMessage(message.replaceAll("%KEY%", securityService.generateKey(uuid).join()))
                    );
                });
                return;
            case "confirm":
                if (!timeStamps.containsKey(uuid)) {
                    messageService.getMessage("command.confirm.error", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                    return;
                }
                final long timeStamp = timeStamps.remove(uuid);
                if (System.currentTimeMillis() <= timeStamp) {
                    messageService.getMessage("totp.create", MessageService.MessageType.INFO).thenAccept(
                            message -> sender.sendMessage(message.replaceAll("%KEY%", securityService.generateKey(uuid).join()))
                    );
                } else {
                    messageService.getMessage("command.confirm.time").thenAccept(sender::sendMessage);
                }
                return;
            case "exit":
                if (!securityService.isSudo(uuid)) {
                    messageService.getMessage("totp.notsudo", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                    return;
                }
                securityService.removeSudo(uuid);
                messageService.getMessage("totp.removesudo", MessageService.MessageType.INFO).thenAccept(sender::sendMessage);
                return;
            case "reset":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
                    return;
                }
                getCommandSystem().sudoAction((Player) sender, player -> playerInfoService.getPlayer(args[1]).thenAccept(cPlayer -> {
                    if (cPlayer == null) {
                        messageService.getMessage("cmd.error.player", MessageService.MessageType.ERROR).thenAccept(
                                message -> player.sendMessage(message.replaceAll("%NAME%", args[1]))
                        );
                        return;
                    }
                    if (!securityService.hasKey(cPlayer.getUuid()).join()) {
                        messageService.getMessage("totp.nokey.others", MessageService.MessageType.ERROR).thenAccept(player::sendMessage);
                        return;
                    }
                    securityService.removeKey(cPlayer.getUuid());
                    messageService.getMessage("totp.removekey", MessageService.MessageType.INFO).thenAccept(player::sendMessage);
                }), 30);
                return;
            default:
                clearAction(((Player) sender).getUniqueId());
                securityService.hasKey(uuid).thenAccept(hasKey -> {
                    if (!hasKey) {
                        messageService.getMessage("totp.nokey", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                        return;
                    }

                    if (securityService.isSudo(uuid)) {
                        messageService.getMessage("totp.alreadysudo", MessageService.MessageType.INFO).thenAccept(sender::sendMessage);
                        return;
                    }
                    final int authNumber;
                    try {
                        StringBuilder builder = new StringBuilder(args[0]);
                        for (int i = 1; i < args.length; i++) {
                            builder.append(args[i]);
                        }
                        authNumber = Integer.valueOf(builder.toString());
                    } catch (final NumberFormatException e) {
                        messageService.getMessage("totp.onlynumbers", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                        return;
                    }
                    final boolean success = securityService.enterSudo(uuid, authNumber).join();
                    messageService.getMessage(success ? "totp.success" : "totp.wrong",
                            success ? MessageService.MessageType.INFO : MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                    if (success) {
                        onSuccess((Player) sender);
                    }
                });
        }
    }

    private void clearAction(final UUID uuid) {
        if (actions.containsKey(uuid)) {
            if (actions.get(uuid).timeStamp < System.currentTimeMillis()) {
                actions.remove(uuid);
            }
        }
    }

    private void onSuccess(final Player player) {
        if (actions.containsKey(player.getUniqueId())) {
            actions.remove(player.getUniqueId()).consumer.accept(player);
        }
    }

    public final boolean sudoAction(final Player player, final Consumer<Player> consumer, final long time) {
        if (securityService.isSudo(player.getUniqueId())) {
            consumer.accept(player);
            return false;
        }
        actions.put(player.getUniqueId(), new SudoAction(consumer, System.currentTimeMillis() + time));
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final String[] args) {
        Validate.notNull(sender);
        Validate.notNull(args);
        if (args.length == 1) {
            final List<String> list = subCommands.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
            if (list.contains("reset") && !sender.hasPermission("totp.reset")) {
                list.remove("reset");
            }
            return list;
        }
        return super.onTabComplete(sender, args);
    }

    public CompletableFuture<Boolean> hasKey(final Player player) {
        return securityService.hasKey(player.getUniqueId());
    }

    private class SudoAction {
        private final Consumer<Player> consumer;
        private final long timeStamp;

        private SudoAction(final Consumer<Player> consumer, final long timeStamp) {
            this.consumer = consumer;
            this.timeStamp = timeStamp;
        }
    }
}
