package pw.eisphoenix.aquacore.misc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "tps",
        description = "Shows the Server TPS",
        permission = "core.tps"
)
public final class TPSCommand extends CCommand {
    private final NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
    @Inject
    private MessageService messageService;

    public TPSCommand() {
        format.setMaximumFractionDigits(2);
    }

    public void onCommand(final CommandSender sender, final String[] args) {
        final double[] tps = Bukkit.spigot().getTPS();
        messageService.getMessage("tps.info", MessageService.MessageType.INFO).thenAccept(
                message -> sender.sendMessage(message
                        .replaceAll("%1%", formatTPS(tps[0]))
                        .replaceAll("%5%", formatTPS(tps[1]))
                        .replaceAll("%15%", formatTPS(tps[2]))
                )
        );
    }

    private String formatTPS(final double tps) {
        return "§" + (tps > 19 ? "a" : tps > 15 ? "e" : "c") + format.format(tps);
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        return ImmutableList.of();
    }
}
