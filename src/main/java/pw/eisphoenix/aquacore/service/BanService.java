package pw.eisphoenix.aquacore.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.concurrent.CompletableFuture;

import static pw.eisphoenix.aquacore.AquaCore.EXECUTOR_SERVICE;

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
        messageService.getMessage("ban.format").thenAccept(message -> dateFormat = new SimpleDateFormat(message));
        CompletableFuture.supplyAsync(() -> banReasonStore.count() < 1, EXECUTOR_SERVICE).thenAccept(result -> {
            if (result) {
                registerDefaultBanReasons();
            }
            updateBanReasons();
        });
    }

    public CompletableFuture<Boolean> isBanned(final CPlayer cPlayer) {
        return getActualBan(cPlayer).thenApply(Objects::nonNull);
    }

    public final CompletableFuture<BanEntry> banPlayer(final CPlayer cPlayer, final UUID source, final BanReason banReason) {
        return getActualBan(cPlayer).thenApply(actualBan -> {
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
                return banEntry;
            }
            return null;
        });
    }

    public final CompletableFuture<Boolean> unbanPlayer(final BanEntry banEntry, final UUID source) {
        return CompletableFuture.supplyAsync(() -> {
            if (banEntry == null || !banEntry.isCurrent()) {
                return false;
            }
            banEntry.setUnbanned(true);
            banEntry.setNotes(banEntry.getNotes().concat("[Unbanned by " + source.toString() + "]"));
            banStore.save(banEntry);
            return true;
        }, EXECUTOR_SERVICE);
    }

    public final CompletableFuture<String> getBanDenyMessage(final BanEntry banEntry) {
        return messageService.getMessage("ban.deny").thenCombine(messageService.getMessage(
                banEntry.getUntil() == -1 ? "ban.forever" : "ban.time"),
                (denyMessage, until) -> denyMessage
                        .replaceAll("%REASON%", banEntry.getBanReason().getDisplayName())
                        .replaceAll("%UNTIL%", banEntry.getUntil() == -1 ?
                                until :
                                until.replaceAll("%DATE%", dateFormat.format(new Date(banEntry.getUntil())))
                        )
        );
    }

    public CompletableFuture<BanEntry> getActualBan(final CPlayer cPlayer) {
        return getBanEntries(cPlayer).thenApply(banEntries -> {
            for (final BanEntry banEntry : banEntries) {
                if (banEntry.isCurrent()) {
                    return banEntry;
                }
            }
            return null;
        });
    }

    private CompletableFuture<List<BanEntry>> getBanEntries(final CPlayer cPlayer) {
        return CompletableFuture.supplyAsync(() ->
                        banStore.find(
                                databaseService.getDatastore().createQuery(BanEntry.class).filter("cPlayer", cPlayer)
                        ).asList()
                , EXECUTOR_SERVICE);
    }

    public CompletableFuture<BanReason> getBanReason(final String name) {
        return CompletableFuture.supplyAsync(() -> {
            for (final BanReason banReason : banReasons) {
                if (banReason.isName(name)) {
                    return banReason;
                }
            }
            return null;
        }, EXECUTOR_SERVICE);
    }

    private void registerDefaultBanReasons() {
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
        CompletableFuture.runAsync(() -> banReasons = banReasonStore.find().asList(), EXECUTOR_SERVICE);
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

    public CompletableFuture<Boolean> isBanned(final InetAddress address) {
        return getActualBan(address).thenApply(Objects::nonNull);
    }

    public CompletableFuture<BanEntry> getActualBan(final InetAddress address) {
        return getBanEntries(address).thenApply(banEntries -> {
            for (final BanEntry banEntry : banEntries) {
                if (banEntry.isCurrent()) {
                    return banEntry;
                }
            }
            return null;
        });
    }

    private CompletableFuture<List<BanEntry>> getBanEntries(final InetAddress address) {
        return CompletableFuture.supplyAsync(() ->
                        banStore.find(
                                databaseService.getDatastore()
                                        .createQuery(BanEntry.class).filter("address", address.getHostAddress())
                        ).asList()
                , EXECUTOR_SERVICE);
    }

    public final CompletableFuture<BanEntry> banIP(final InetAddress address, final UUID source, final BanReason banReason) {
        return getActualBan(address).thenApply(actualBan -> {
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
                return banEntry;
            }
            return null;
        });

    }
}
