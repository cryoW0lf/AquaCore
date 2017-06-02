package pw.eisphoenix.aquacore.cmd;

import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class CommandImpl extends Command {
    @Inject
    private MessageService messageService;
    private final CCommand cCommand;
    private final CommandInfo commandInfo;

    public CommandImpl(final CCommand cCommand, final CommandInfo commandInfo) {
        super(commandInfo.name(), commandInfo.description(),
                commandInfo.usage().replaceAll("%CMD%", commandInfo.name()), Arrays.asList(commandInfo.aliase()));
        this.cCommand = cCommand;
        this.commandInfo = commandInfo;
        DependencyInjector.inject(cCommand);
        DependencyInjector.inject(this);

    }

    @Override
    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        try {
            if (!sender.hasPermission(commandInfo.permission())) {
                sender.sendMessage(
                        messageService.getMessage("cmd.error.perm", MessageService.MessageType.ERROR)
                );
                return true;
            }
            if (sender instanceof Player) {
                if (!commandInfo.options().forPlayer()) {
                    sender.sendMessage(
                            messageService.getMessage("cmd.sender.player", MessageService.MessageType.ERROR)
                    );
                } else {
                    cCommand.onCommand(sender, args);
                }
                return true;
            }
            if (sender instanceof ConsoleCommandSender) {
                if (!commandInfo.options().forConsole()) {
                    sender.sendMessage(
                            messageService.getMessage("cmd.sender.console", MessageService.MessageType.ERROR)
                    );
                } else {
                    cCommand.onCommand(sender, args);
                }
                return true;
            }
            if (!commandInfo.options().forOthers()) {
                sender.sendMessage(
                        messageService.getMessage("cmd.sender.others", MessageService.MessageType.ERROR)
                );
            } else {
                cCommand.onCommand(sender, args);
            }
        } catch (final Exception e) {
            sender.sendMessage(
                    messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR)
            );
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        return cCommand.onTabComplete(sender, args);
    }
}
