package pw.eisphoenix.aquacore.cmd;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public abstract class CCommand {
    private String name = "";
    private String usage = "";
    private CommandSystem commandSystem;

    public abstract void onCommand(final CommandSender sender, final String[] args);

    public List<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            return ImmutableList.of();
        }
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName)
                .filter(p -> p.toLowerCase().startsWith(args[args.length -1].toLowerCase())).collect(Collectors.toList());
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final String getUsage() {
        return usage;
    }

    public final void setUsage(final String usage) {
        this.usage = usage.replaceAll("%CMD%", name);
    }

    public final CommandSystem getCommandSystem() {
        return commandSystem;
    }

    public final void setCommandSystem(final CommandSystem commandSystem) {
        this.commandSystem = commandSystem;
    }

    /*protected void sudoAction(final Player player, final Consumer<UUID> consumer) {
        if (securityService.isSudo(player.getUniqueId())) {
            consumer.accept(player.getUniqueId());
        } else {
            player
        }
    }*/
}
