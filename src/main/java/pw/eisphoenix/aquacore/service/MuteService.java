package pw.eisphoenix.aquacore.service;

import pw.eisphoenix.aquacore.chat.Mute;
import pw.eisphoenix.aquacore.dbstore.MuteDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;

import java.util.List;
import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class MuteService implements InjectionHook {
    private final static UUID SYSTEM_UUID = UUID.nameUUIDFromBytes("SYSTEM".getBytes());
    @Inject
    private DatabaseService databaseService;
    private MuteDAO muteStore;

    public final Mute mutePlayer(final UUID uuid, UUID source, final long length, final String reason) {
        if (source == null) {
            source = SYSTEM_UUID;
        }
        final Mute mute = getCurrentMute(uuid);
        if (mute != null) {
            if (mute.getLength() + mute.getTimestamp() > System.currentTimeMillis() + length) {
                return null;
            }
            mute.setValid(false);
        }
        final Mute aMute = new Mute(uuid, source, length, reason);
        if (mute != null) {
            mute.setNotes(mute.getNotes().concat("[Replaced by " + aMute.getUuid() + "]"));
            muteStore.save(mute);
        }
        muteStore.save(aMute);
        return aMute;
    }

    public final boolean unmutePlayer(final UUID uuid, UUID source) {
        if (source == null) {
            source = SYSTEM_UUID;
        }
        final Mute mute = getCurrentMute(uuid);
        if (mute == null) {
            return false;
        }
        mute.setValid(false);
        mute.setNotes(mute.getNotes().concat("[Unmute by " + source + "]"));
        muteStore.save(mute);
        return true;
    }

    public final boolean isMuted(final UUID uuid) {
        return getCurrentMute(uuid) != null;
    }

    public final Mute getCurrentMute(final UUID uuid) {
        final List<Mute> mutes = getMutes(uuid);

        for (final Mute mute : mutes) {
            if (mute.isCurrent()) {
                return mute;
            }
        }
        return null;
    }

    public final List<Mute> getMutes(final UUID uuid) {
        return muteStore.find(databaseService.getDatastore().createQuery(Mute.class).filter("player", uuid))
                .asList();
    }

    @Override
    public void postInjection() {
        muteStore = new MuteDAO(Mute.class, databaseService.getDatastore());
    }
}
