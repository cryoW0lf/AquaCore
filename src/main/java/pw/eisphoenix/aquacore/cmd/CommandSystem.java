package pw.eisphoenix.aquacore.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.ban.*;
import pw.eisphoenix.aquacore.chat.MuteCommand;
import pw.eisphoenix.aquacore.chat.UnmuteCommand;
import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.misc.PingCommand;
import pw.eisphoenix.aquacore.misc.TPSCommand;
import pw.eisphoenix.aquacore.permission.RankCommand;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.totp.TOTPCommand;

import java.util.function.Consumer;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class CommandSystem {
    @Inject
    private MessageService messageService;
    private SimpleCommandMap simpleCommandMap;
    private TOTPCommand totpCommand = new TOTPCommand();

    public CommandSystem() {
        DependencyInjector.inject(this);
        simpleCommandMap = (SimpleCommandMap) Bukkit.getCommandMap();

        registerCommand(totpCommand);
        registerCommand(new BanCommand());
        registerCommand(new BanIPCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new UnbanIPCommand());
        registerCommand(new KickCommand());
        registerCommand(new MuteCommand());
        registerCommand(new UnmuteCommand());
        registerCommand(new RankCommand());
        registerCommand(new TPSCommand());
        registerCommand(new PingCommand());
    }

    public final void registerCommand(final CCommand cCommand) {
        final CommandInfo commandInfo = cCommand.getClass().getAnnotationsByType(CommandInfo.class)[0];
        cCommand.setName(commandInfo.name());
        cCommand.setUsage(commandInfo.usage());
        cCommand.setCommandSystem(this);

        simpleCommandMap.register("core",
                new CommandImpl(cCommand, commandInfo)
        );
    }

    public final void sudoAction(final Player player, Consumer<Player> consumer, final int time) {
        if (!player.hasPermission("totp.use")) {
            consumer.accept(null);
            return;
        }

        totpCommand.hasKey(player).thenAccept(hasKey -> {
            if (!hasKey) {
                messageService.getMessage("totp.nokey", MessageService.MessageType.ERROR).thenAccept(player::sendMessage);
                consumer.accept(null);
                return;
            }

            if (totpCommand.sudoAction(player, consumer, time * 1000)) {
                messageService.getMessage("totp.confirm", MessageService.MessageType.INFO).thenAccept(
                        message -> player.sendMessage(message
                                .replaceAll("%MESSAGE%", "/code <Code>")
                                .replaceAll("%TIME%", time + " Sekunden")
                        )
                );
            }
        });
    }

}
