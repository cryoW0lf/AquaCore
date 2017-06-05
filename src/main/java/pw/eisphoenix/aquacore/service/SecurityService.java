package pw.eisphoenix.aquacore.service;

import pw.eisphoenix.aquacore.dbstore.TOTPDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.totp.TOTPEntry;
import pw.eisphoenix.aquacore.util.TimeBasedOneTimePasswordUtil;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class SecurityService implements InjectionHook {
    private final static long VALIDE_TIME = 600000;
    private Map<UUID, Long> sudoMode = new HashMap<>();
    @Inject
    private DatabaseService databaseService;
    private TOTPDAO totpStore;

    public final CompletableFuture<String> generateKey(final UUID uuid) {
        return getEntry(uuid).thenApply(totpEntry -> {
            if (totpEntry == null) {
                totpEntry = new TOTPEntry(uuid, TimeBasedOneTimePasswordUtil.generateBase32Secret());
                totpStore.save(totpEntry);
                return totpEntry.getKey();
            }
            totpEntry.setKey(TimeBasedOneTimePasswordUtil.generateBase32Secret());
            totpStore.save(totpEntry);
            return totpEntry.getKey();
        });
    }

    public final CompletableFuture<Boolean> hasKey(final UUID uuid) {
        return getEntry(uuid).thenApply(Objects::nonNull);
    }

    public final CompletableFuture<Boolean> enterSudo(final UUID uuid, final int authNumber) {
        return getEntry(uuid).thenApply(totpEntry -> {
            if (totpEntry == null) {
                return false;
            }
            try {
                final boolean success = TimeBasedOneTimePasswordUtil.validateCurrentNumber(totpEntry.getKey(),
                        authNumber, 1000);
                if (success) {
                    sudoMode.put(uuid, System.currentTimeMillis());
                }
                return success;
            } catch (final GeneralSecurityException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public final boolean isSudo(final UUID uuid) {
        if (!sudoMode.containsKey(uuid)) {
            return false;
        }
        final long timeStamp = sudoMode.get(uuid);
        if (System.currentTimeMillis() > timeStamp + VALIDE_TIME) {
            return false;
        }
        sudoMode.replace(uuid, System.currentTimeMillis());
        return true;
    }

    public final boolean removeSudo(final UUID uuid) {
        return sudoMode.remove(uuid) != null;
    }

    private CompletableFuture<TOTPEntry> getEntry(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> totpStore.findOne("uuid", uuid));
    }

    @Override
    public void postInjection() {
        totpStore = new TOTPDAO(TOTPEntry.class, databaseService.getDatastore());
    }

    public void removeKey(final UUID uuid) {
        getEntry(uuid).thenAccept(totpEntry -> totpStore.delete(totpEntry));
    }
}
