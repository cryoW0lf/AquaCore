package pw.eisphoenix.aquacore.chat;

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
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.MuteService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "mute",
        description = "Mutes a Player",
        usage = "/%CMD% <player> <time<s/m/h/d>/0> <reason>",
        permission = "core.mute"
)
public final class MuteCommand extends CCommand {
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
    private final Pattern LENGTHPATTERN = Pattern.compile("\\d+(\\.\\d+)?");
    private final Pattern UNITPATTERN = Pattern.compile("([sSmMhHdDpP])");
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private MuteService muteService;
    @Inject
    private MessageService messageService;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }

        final CPlayer cPlayer = playerInfoService.getPlayer(args[0]);
        if (cPlayer == null) {
            sender.sendMessage(
                    messageService.getMessage("cmd.error.player", MessageService.MessageType.ERROR)
                            .replaceAll("%NAME%", args[0])
            );
            return;
        }

        if (!sender.hasPermission("mute.exempt") && cPlayer.getRank().hasPermissions("mute.exempt")) {
            sender.sendMessage(
                    messageService.getMessage("mute.exempt", MessageService.MessageType.ERROR)
            );
            return;
        }
        final String timeS = args[1];
        final Matcher lengthMatcher = LENGTHPATTERN.matcher(timeS);
        if (!timeS.matches("^(\\d+(\\.\\d+)?([sSmMhHdDpP])|p?)*") || !lengthMatcher.find()) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }
        System.out.println("timeS = " + timeS);
        final double length = !timeS.startsWith("p") ? Double.valueOf(lengthMatcher.group()) : -1;
        if (length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }
        final Matcher matcher = UNITPATTERN.matcher(timeS);
        String unit = matcher.find() ? matcher.group() : "n";
        final long totalLength = (long) (length *
                (unit.equalsIgnoreCase("s") ? 1000 :
                        unit.equalsIgnoreCase("m") ? 1000 * 60 :
                                unit.equalsIgnoreCase("h") ? 1000 * 60 * 60 :
                                        unit.equalsIgnoreCase("d") ? 1000 * 60 * 60 * 24 : 0));
        final StringBuilder builder = new StringBuilder(args[2]);
        for (int i = 3; i < args.length; i++) {
            builder.append(" ").append(args[i]);
        }
        UUID uuid = null;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        }
        final Mute mute = muteService.mutePlayer(cPlayer.getUuid(), uuid, totalLength, builder.toString());
        if (mute == null) {
            sender.sendMessage(
                    messageService.getMessage("mute.muted", MessageService.MessageType.ERROR)
            );
            return;
        }
        final Player player = Bukkit.getPlayer(cPlayer.getUuid());
        final boolean isOne = length == 1;
        if (player != null) {
            player.sendMessage(
                    messageService.getMessage("mute.message.onmute", MessageService.MessageType.WARNING)
                            .replaceAll("%NAME%", sender.getName())
                            .replaceAll("%TIME%", unit.equalsIgnoreCase("p") ?
                                    messageService.getMessage("mute.forever") :
                                    numberFormat.format(length) + " " +
                                            messageService.getMessage("unit." +
                                                    (unit.equalsIgnoreCase("s") ? "second" :
                                                            unit.equalsIgnoreCase("m") ? "minute" :
                                                                    unit.equalsIgnoreCase("h") ? "hour" :
                                                                            unit.equalsIgnoreCase("d") ? "day" : "nope") + (isOne ? "s" : "")
                                            ))
                            .replaceAll("%REASON%", builder.toString())
            );
        }
        sender.sendMessage(
                messageService.getMessage("mute.success", MessageService.MessageType.INFO)
                        .replaceAll("%NAME%", cPlayer.getActualUsername())
                        .replaceAll("%TIME%", unit.equalsIgnoreCase("p") ?
                                messageService.getMessage("mute.forever") :
                                numberFormat.format(length) + " " +
                                        messageService.getMessage("unit." +
                                                (unit.equalsIgnoreCase("s") ? "second" :
                                                        unit.equalsIgnoreCase("m") ? "minute" :
                                                                unit.equalsIgnoreCase("h") ? "hour" :
                                                                        unit.equalsIgnoreCase("d") ? "day" : "nope") + (isOne ? "s" : "")
                                        ))
                        .replaceAll("%REASON%", builder.toString())
        );
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1) {
            return super.onTabComplete(sender, args);
        }
        return ImmutableList.of();
    }
}
