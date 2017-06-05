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
        banService.isBanned(event.getAddress()).thenApply(result -> {
            if (!result) {
                return true;
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banService.getBanDenyMessage(
                    banService.getActualBan(event.getAddress()).join()
            ).join());
            return false;
        }).thenAccept(result -> {
            if (!result) {
                return;
            }
            final CPlayer cPlayer = playerInfoService.getPlayer(event.getUniqueId(), event.getName()).join();
            if (banService.isBanned(cPlayer).join()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banService.getBanDenyMessage(
                        banService.getActualBan(cPlayer).join()
                ).join());
            }
        });
    }
}
