package pw.eisphoenix.aquacore.service;

import pw.eisphoenix.aquacore.totp.TOTPEntry;
import pw.eisphoenix.aquacore.dbstore.TOTPDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.util.TimeBasedOneTimePasswordUtil;

import java.security.GeneralSecurityException;
import java.util.*;

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

    public final String generateKey(final UUID uuid) {
        TOTPEntry totpEntry = getEntry(uuid);
        if (totpEntry == null) {
            totpEntry = new TOTPEntry(uuid, TimeBasedOneTimePasswordUtil.generateBase32Secret());
            totpStore.save(totpEntry);
            return totpEntry.getKey();
        }
        totpEntry.setKey(TimeBasedOneTimePasswordUtil.generateBase32Secret());
        totpStore.save(totpEntry);
        return totpEntry.getKey();
    }

    public final boolean hasKey(final UUID uuid) {
        return getEntry(uuid) != null;
    }

    public final boolean enterSudo(final UUID uuid, final int authNumber) {
        TOTPEntry totpEntry = getEntry(uuid);
        if (totpEntry == null)
            return false;
        try {
            final boolean success = TimeBasedOneTimePasswordUtil.validateCurrentNumber(totpEntry.getKey(),
                    authNumber, 1000);
            if (success) {
                sudoMode.put(uuid, System.currentTimeMillis());
            }
            return success;
        } catch (final GeneralSecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public final boolean isSudo(final UUID uuid) {
        if (!sudoMode.containsKey(uuid)) {
            return false;
        }
        final long timeStamp = sudoMode.get(uuid);
        if (System.currentTimeMillis() > timeStamp + VALIDE_TIME)
            return false;
        sudoMode.replace(uuid, System.currentTimeMillis());
        return true;
    }

    public final boolean removeSudo(final UUID uuid) {
        return sudoMode.remove(uuid) != null;
    }

    private TOTPEntry getEntry(final UUID uuid) {
        return totpStore.findOne("uuid", uuid);
    }

    @Override
    public void postInjection() {
        totpStore = new TOTPDAO(TOTPEntry.class, databaseService.getDatastore());
    }

    public void removeKey(final UUID uuid) {
        totpStore.delete(getEntry(uuid));
    }
}
