package pw.eisphoenix.aquacore.permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import pw.eisphoenix.aquacore.AquaCore;
import pw.eisphoenix.aquacore.CPlayer;
import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.PlayerInfoService;
import pw.eisphoenix.aquacore.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class PermissionSystem implements Listener {
    @Inject
    private PlayerInfoService playerInfoService;
    private Field permissionsField;

    public PermissionSystem() {
        DependencyInjector.inject(this);
        try {
            final Class<?> clazz = ReflectionUtil.getCBClass("entity.CraftHumanEntity");
            assert clazz != null;
            permissionsField = clazz.getDeclaredField("perm");
            permissionsField.setAccessible(true);
            ReflectionUtil.setFinal(permissionsField, false);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(AquaCore.getInstance(), () -> {
            updatePermissions(event.getPlayer());
        });
    }

    public void updatePermissions(final Player player) {
        try {
            permissionsField.set(player, new PermissibleBase(player));
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public class PermissibleBase extends org.bukkit.permissions.PermissibleBase {
        private CPlayer cPlayer;
        //private Map<String, Boolean> cache = new HashMap<>();

        public PermissibleBase(final Player player) {
            super(player);
            cPlayer = playerInfoService.getPlayer(player).join();
        }

        @Override
        public boolean hasPermission(final String inName) {
            return cPlayer.getRank().hasPermissions(inName);
            //return cache.getOrDefault(inName, hasPermI(inName));
        }

        @Override
        public boolean isPermissionSet(String name) {
            return true;
        }

        @Override
        public boolean isPermissionSet(Permission perm) {
            return isPermissionSet(perm.getName());
        }

        @Override
        public boolean hasPermission(Permission perm) {
            return hasPermission(perm.getName());
        }

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            return super.getEffectivePermissions();
        }


    }
}
