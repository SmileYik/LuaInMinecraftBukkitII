package org.eu.smileyik.luaInMinecraftBukkitII;

import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.command.RootCommand;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandRegister;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.keplerproject.luajava.LuaState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LuaInMinecraftBukkit extends JavaPlugin {
    public static final String NATIVES_FOLDER = "natives";
    public static final String LUA_STATE_FOLDER = "luaState";
    public static final String LUA_LIB_FOLDER = "luaLibrary";

    private static final String[] FOLDERS = new String[] {
            LUA_STATE_FOLDER,
            NATIVES_FOLDER,
            LUA_LIB_FOLDER,
            "scripts"
    };

    private static final AtomicBoolean LOADED_NATIVES = new AtomicBoolean(false);
    private static LuaInMinecraftBukkit plugin;
    @Getter
    private ILuaStateManager luaStateManager = null;

    public LuaInMinecraftBukkit() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            CommandService.newInstance(
                    LuaCommandRegister.DEFAULT_TRANSLATOR,
                    LuaCommandRegister.DEFAULT_FORMAT,
                    RootCommand.class
            ).registerToBukkit(this);
        } catch (Exception e) {
            getLogger().warning("Cannot register command!");
            DebugLogger.debug(e);
        }
        reload();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (luaStateManager != null) {
            luaStateManager.close();
            luaStateManager = null;
        }
        try {
            DebugLogger.closeLogger();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        for (String folder : FOLDERS) {
            if (!new File(getDataFolder(), folder).exists()) {
                new File(getDataFolder(), folder).mkdirs();
            }
        }
        if (!new File(getDataFolder(), "config.json").exists()) {
            saveResource("config.json", false);
        }

        Config config;
        try {
            config = loadConfig();
            if (config.isDebug()) {
                DebugLogger.closeLogger();
                DebugLogger.init(getLogger(), new File(getDataFolder(), "debug.log"), 0xFFFF);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // load native library.
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            if (LOADED_NATIVES.compareAndSet(false, true)) {
                getLogger().info("Loading lua native libraries...");
                try {
                    NativeLoader.load(config);
                } catch (Exception e) {
                    getPluginLoader().disablePlugin(this);
                    throw new RuntimeException("Could not init lua state event: ", e);
                }
                getLogger().info("Successfully loaded lua native libraries, lua version: " +
                        LuaState.LUA_VERSION);
            }
            // after loaded then init plugin.
            getServer().getScheduler().runTask(this, () -> init(config));
        });
    }

    private void init(Config config) {
        luaStateManager = new LuaStateManager(config);
    }

    public Config loadConfig() throws IOException {
        List<String> strings = Files.readAllLines(new File(getDataFolder(), "config.json").toPath());
        String json = String.join("\n", strings);
        json = JsonUtil.stripComments(json);
        return new Gson().fromJson(json, Config.class);
    }

    public static LuaInMinecraftBukkit instance() {
        return plugin;
    }

    public File getLuaStateFolder() {
        return new File(getDataFolder(), LUA_STATE_FOLDER);
    }
}
