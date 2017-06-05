package pw.eisphoenix.aquacore.service;

import pw.eisphoenix.aquacore.chat.Mute;
import pw.eisphoenix.aquacore.dbstore.MuteDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    public final CompletableFuture<Mute> mutePlayer(final UUID uuid, final UUID source, final long length, final String reason) {
        final UUID finalSource = source == null ? SYSTEM_UUID : source;
        return getCurrentMute(uuid).thenApply(actualMute -> {
            if (actualMute != null) {
                if (actualMute.getLength() + actualMute.getTimestamp() > System.currentTimeMillis() + length) {
                    return null;
                }
                actualMute.setValid(false);
                muteStore.save(actualMute);
            }
            final Mute mute = new Mute(uuid, source, length, reason);
            if (actualMute != null) {
                mute.setNotes(mute.getNotes().concat("[Replaced by " + mute.getUuid() + "]"));
                muteStore.save(mute);
            }
            muteStore.save(mute);
            return mute;
        });
    }

    public final CompletableFuture<Boolean> unmutePlayer(final UUID uuid, final UUID source) {
        final UUID finalSource = source == null ? SYSTEM_UUID : source;
        return getCurrentMute(uuid).thenApply(mute -> {
            if (mute == null) {
                return false;
            }
            mute.setValid(false);
            mute.setNotes(mute.getNotes().concat("[Unmute by " + finalSource + "]"));
            muteStore.save(mute);
            return true;
        });
    }

    public final CompletableFuture<Boolean> isMuted(final UUID uuid) {
        return getCurrentMute(uuid).thenApply(Objects::nonNull);
    }

    public final CompletableFuture<Mute> getCurrentMute(final UUID uuid) {
        return getMutes(uuid).thenApply(mutes -> {
            for (final Mute mute : mutes) {
                if (mute.isCurrent()) {
                    return mute;
                }
            }
            return null;
        });
    }

    public final CompletableFuture<List<Mute>> getMutes(final UUID uuid) {
        return CompletableFuture.supplyAsync(() ->
                muteStore.find(databaseService.getDatastore().createQuery(Mute.class).filter("player", uuid))
                        .asList()
        );
    }

    @Override
    public void postInjection() {
        muteStore = new MuteDAO(Mute.class, databaseService.getDatastore());
    }
}
