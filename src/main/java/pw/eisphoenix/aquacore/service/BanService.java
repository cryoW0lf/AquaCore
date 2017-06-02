package pw.eisphoenix.aquacore.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.AquaCore;
import pw.eisphoenix.aquacore.CPlayer;
import pw.eisphoenix.aquacore.ban.BanEntry;
import pw.eisphoenix.aquacore.ban.BanReason;
import pw.eisphoenix.aquacore.dbstore.BanDAO;
import pw.eisphoenix.aquacore.dbstore.BanReasonDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.observer.ClearCacheNotification;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class BanService implements InjectionHook, Observer {
    @Inject
    private DatabaseService databaseService;
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private ObserverService observerService;
    @Inject
    private MessageService messageService;
    private BanDAO banStore;
    private BanReasonDAO banReasonStore;
    private DateFormat dateFormat;
    private List<BanReason> banReasons;

    @Override
    public final void postInjection() {
        banStore = new BanDAO(BanEntry.class, databaseService.getDatastore());
        banReasonStore = new BanReasonDAO(BanReason.class, databaseService.getDatastore());
        observerService.addObserver(ClearCacheNotification.class, this);
        dateFormat = new SimpleDateFormat(messageService.getMessage("ban.format"));
        if (banReasonStore.count() < 1) {
            registerDefaultBanReasons();
        }
        updateBanReasons();
    }

    public boolean isBanned(final CPlayer cPlayer) {
        return getActualBan(cPlayer) != null;
    }

    public final BanEntry banPlayer(final CPlayer cPlayer, final UUID source, final BanReason banReason) {
        final BanEntry actualBan = getActualBan(cPlayer);
        final int often = (int) banStore.count(databaseService.getDatastore().createQuery(BanEntry.class)
                .filter("cPlayer", cPlayer).filter("banReason", banReason)
                .filter("unbanned", false));
        if (actualBan == null || actualBan.getBanReason().getDurations(actualBan.getOften()) <= banReason.getDurations(often)) {
            final BanEntry banEntry = new BanEntry(source, cPlayer, banReason, often);
            if (actualBan != null) {
                actualBan.setNotes(actualBan.getNotes().concat("[Replaced with " + banEntry.getUuid() + "]"));
                actualBan.setValidBan(false);
            }
            banStore.save(banEntry);
            final Player player = Bukkit.getPlayer(cPlayer.getUuid());
            if (player != null) {
                Bukkit.getScheduler().runTask(AquaCore.getInstance(),
                        () -> player.kickPlayer(getBanDenyMessage(banEntry)));
            }
            return banEntry;
        }
        return null;
    }

    public final boolean unbanPlayer(final BanEntry banEntry, final UUID source) {
        if (banEntry == null || !banEntry.isCurrent()) {
            return false;
        }
        banEntry.setUnbanned(true);
        banEntry.setNotes(banEntry.getNotes().concat("[Unbanned by " + source.toString() + "]"));
        banStore.save(banEntry);
        return true;
    }

    public final String getBanDenyMessage(final BanEntry banEntry) {
        return messageService.getMessage("ban.deny")
                .replaceAll("%REASON%", banEntry.getBanReason().getDisplayName())
                .replaceAll("%UNTIL%", banEntry.getUntil() == -1 ? messageService.getMessage("ban.forever") :
                        messageService.getMessage("ban.time")
                                .replaceAll(
                                        "%DATE%", dateFormat.format(new Date(banEntry.getUntil()))
                                )
                );
    }

    public BanEntry getActualBan(final CPlayer cPlayer) {
        final List<BanEntry> banEntries = getBanEntries(cPlayer);
        for (final BanEntry banEntry : banEntries) {
            if (banEntry.isCurrent()) {
                return banEntry;
            }
        }
        return null;
    }

    private List<BanEntry> getBanEntries(final CPlayer cPlayer) {
        return banStore.find(
                databaseService.getDatastore().createQuery(BanEntry.class).filter("cPlayer", cPlayer)
        ).asList();
    }

    public BanReason getBanReason(final String name) {
        for (final BanReason banReason : banReasons) {
            if (banReason.isName(name)) {
                return banReason;
            }
        }
        return null;
    }

    private void registerDefaultBanReasons() {
        System.out.println("Hey");
        final JsonObject rootObject = AquaCore.JSON_PARSER.parse(new InputStreamReader(
                getClass().getResourceAsStream("/banReasons.json")
        )).getAsJsonObject();
        JsonObject reasonObject;
        for (final Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
            reasonObject = entry.getValue().getAsJsonObject();
            banReasonStore.save(new BanReason(entry.getKey(),
                    AquaCore.GSON.fromJson(reasonObject.get("aliase"), String[].class),
                    reasonObject.getAsJsonPrimitive("displayName").getAsString(),
                    AquaCore.GSON.fromJson(reasonObject.get("durations"), int[].class),
                    reasonObject.getAsJsonPrimitive("permanent").getAsBoolean()
            ));
        }
    }

    private void updateBanReasons() {
        banReasons = banReasonStore.find().asList();
    }

    public final List<String> getBanReasons(final String s) {
        final List<String> result = new LinkedList<>();
        for (final BanReason banReason : banReasons) {
            if (banReason.getName().startsWith(s)) {
                result.add(banReason.getName());
                continue;
            }
            for (final String alias : banReason.getAliase()) {
                if (alias.startsWith(s)) {
                    result.add(alias);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void update(final Observable o, final Object arg) {
        if (arg instanceof ClearCacheNotification) {
            updateBanReasons();
        }
    }

    public boolean isBanned(final InetAddress address) {
        return getActualBan(address) != null;
    }

    public BanEntry getActualBan(final InetAddress address) {
        final List<BanEntry> banEntries = getBanEntries(address);
        for (final BanEntry banEntry : banEntries) {
            if (banEntry.isCurrent()) {
                return banEntry;
            }
        }
        return null;
    }

    private List<BanEntry> getBanEntries(final InetAddress address) {
        return banStore.find(
                databaseService.getDatastore().createQuery(BanEntry.class)
                        .filter("address", address.getHostAddress())
        ).asList();
    }

    public final BanEntry banIP(final InetAddress address, final UUID source, final BanReason banReason) {
        final BanEntry actualBan = getActualBan(address);
        final int often = (int) banStore.count(databaseService.getDatastore().createQuery(BanEntry.class)
                .filter("banReason", banReason).filter("address", address.getHostAddress())
                .filter("unbanned", false));
        if (actualBan == null || actualBan.getBanReason().getDurations(actualBan.getOften()) <= banReason.getDurations(often)) {
            final BanEntry banEntry = new BanEntry(source, address.getHostAddress(), banReason, often);
            if (actualBan != null) {
                actualBan.setNotes(actualBan.getNotes().concat("[Replaced with " + banEntry.getUuid() + "]"));
                actualBan.setValidBan(false);
            }
            banStore.save(banEntry);
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.getAddress().getAddress().equals(address)) {
                    Bukkit.getScheduler().runTask(AquaCore.getInstance(),
                            () -> player.kickPlayer(getBanDenyMessage(banEntry)));
                }
            }
            return banEntry;
        }
        return null;
    }
}
