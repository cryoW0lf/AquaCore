package pw.eisphoenix.aquacore.ban;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pw.eisphoenix.aquacore.CPlayer;
import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.BanService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class BanSystem implements Listener {
    @Inject
    private BanService banService;
    @Inject
    private PlayerInfoService playerInfoService;

    public BanSystem() {
        DependencyInjector.inject(this);
    }

    @EventHandler
    public void onJoin(final AsyncPlayerPreLoginEvent event) {
        if (banService.isBanned(event.getAddress())) {
            final BanEntry banEntry = banService.getActualBan(event.getAddress());
            assert banEntry != null;
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banService.getBanDenyMessage(banEntry));
        }
        final CPlayer cPlayer = playerInfoService.getPlayer(event.getUniqueId(), event.getName());
        if (banService.isBanned(cPlayer)) {
            final BanEntry banEntry = banService.getActualBan(cPlayer);
            assert banEntry != null;
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banService.getBanDenyMessage(banEntry));

        }
    }
}
