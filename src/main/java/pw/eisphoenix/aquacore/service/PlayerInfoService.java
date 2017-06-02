package pw.eisphoenix.aquacore.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.CPlayer;
import pw.eisphoenix.aquacore.dbstore.PlayerDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;

import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class PlayerInfoService implements InjectionHook {
    @Inject
    private DatabaseService databaseService;
    @Inject
    private PermissionService permissionService;
    private PlayerDAO playerStore;

    @Override
    public void postInjection() {
        playerStore = new PlayerDAO(CPlayer.class, databaseService.getDatastore());
    }

    public final CPlayer getPlayer(final Player player) {
        return getPlayer(player.getUniqueId());
    }

    public final CPlayer getPlayer(final UUID uuid) {
        CPlayer cPlayer = playerStore.findOne("uuid", uuid);
        if (cPlayer == null) {
            cPlayer = new CPlayer(Bukkit.getPlayer(uuid).getName(), uuid, permissionService.getRank());
            playerStore.save(cPlayer);
        }
        return cPlayer;
    }

    public final void savePlayer(final CPlayer cPlayer) {
        playerStore.save(cPlayer);
    }

    public final CPlayer getPlayer(final UUID uuid, final String name) {
        CPlayer cPlayer = playerStore.findOne("uuid", uuid);
        if (cPlayer == null) {
            cPlayer = new CPlayer(name, uuid, permissionService.getRank());
            playerStore.save(cPlayer);
        }
        return cPlayer;
    }

    public CPlayer getPlayer(final String name) {
        final CPlayer cPlayer = playerStore.findOne("actualUsername", name);
        if (cPlayer != null) {
            return cPlayer;
        }
        final Player player = Bukkit.getPlayer(name);
        if (player != null) {
            return playerStore.findOne("actualUsername", player.getName());
        }
        return null;
    }
}
