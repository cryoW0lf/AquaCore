package pw.eisphoenix.aquacore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import pw.eisphoenix.aquacore.ban.BanSystem;
import pw.eisphoenix.aquacore.chat.ChatSystem;
import pw.eisphoenix.aquacore.cmd.CommandSystem;
import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.permission.PermissionSystem;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class AquaCore extends JavaPlugin implements Listener {
    public final static Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    public final static Gson GSON = new Gson();
    public final static JsonParser JSON_PARSER = new JsonParser();
    private static AquaCore instance;
    private Configuration configuration;
    private BanSystem banSystem;
    private PermissionSystem permissionSystem;
    private CommandSystem commandSystem;
    private ChatSystem chatSystem;

    @Override
    public void onEnable() {
        instance = this;
        configuration = Configuration.load();
        DependencyInjector.inject(this);
        permissionSystem = new PermissionSystem();
        banSystem = new BanSystem();
        commandSystem = new CommandSystem();
        chatSystem = new ChatSystem();
        Bukkit.getPluginManager().registerEvents(permissionSystem, this);
        Bukkit.getPluginManager().registerEvents(banSystem, this);
        Bukkit.getPluginManager().registerEvents(chatSystem, this);

    }

    @Override
    public void onDisable() {
        try {
            configuration.save();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static AquaCore getInstance() {
        return instance;
    }

    public static Configuration getConfiguration() {
        return instance.configuration;
    }

    public final BanSystem getBanSystem() {
        return banSystem;
    }

    public final PermissionSystem getPermissionSystem() {
        return permissionSystem;
    }

    public final CommandSystem getCommandSystem() {
        return commandSystem;
    }
}
