package pw.eisphoenix.aquacore.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;

import java.util.Arrays;
import java.util.List;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class CommandImpl extends Command {
    private final CCommand cCommand;
    private final CommandInfo commandInfo;
    @Inject
    private MessageService messageService;

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
                messageService.getMessage("cmd.error.perm", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                return true;
            }
            if (sender instanceof Player) {
                if (!commandInfo.options().forPlayer()) {
                    messageService.getMessage("cmd.sender.player", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                } else {
                    cCommand.onCommand(sender, args);
                }
                return true;
            }
            if (sender instanceof ConsoleCommandSender) {
                if (!commandInfo.options().forConsole()) {
                    messageService.getMessage("cmd.sender.console", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
                } else {
                    cCommand.onCommand(sender, args);
                }
                return true;
            }
            if (!commandInfo.options().forOthers()) {
                messageService.getMessage("cmd.sender.others", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
            } else {
                cCommand.onCommand(sender, args);
            }
        } catch (final Exception e) {
            messageService.getMessage("cmd.error.unknown", MessageService.MessageType.ERROR).thenAccept(sender::sendMessage);
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        return cCommand.onTabComplete(sender, args);
    }
}
