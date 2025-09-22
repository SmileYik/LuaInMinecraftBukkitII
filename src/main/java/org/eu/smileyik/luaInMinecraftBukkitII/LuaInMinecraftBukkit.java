package org.eu.smileyik.luaInMinecraftBukkitII;

import com.google.gson.Gson;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.command.RootCommand;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandRegister;
import org.eu.smileyik.luaInMinecraftBukkitII.scheduler.Scheduler;
import org.eu.smileyik.luaInMinecraftBukkitII.util.BStatsMetrics;
import org.eu.smileyik.luaInMinecraftBukkitII.util.ResourcesExtractor;
import org.eu.smileyik.luajava.LuaJavaAPI;
import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.reflect.ReflectUtil;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class LuaInMinecraftBukkit extends JavaPlugin {
    public static final String NATIVES_FOLDER = "natives";
    public static final String LUA_STATE_FOLDER = "luaState";
    public static final String LUA_LIB_FOLDER = "luaLibrary";

    private static final int BSTATS_CODE = 26298;
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
    @Getter
    private final Scheduler scheduler = Scheduler.newInstance();
    private Metrics metrics;

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
        close();
        HandlerList.unregisterAll(this);
        scheduler.cancel(this);
    }

    private void close() {
        if (luaStateManager != null) {
            luaStateManager.close();
            luaStateManager = null;
        }
        try {
            DebugLogger.closeLogger();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (metrics != null) {
            metrics.shutdown();
        }
    }

    public void reload() {
        close();
        for (String folder : FOLDERS) {
            if (!new File(getDataFolder(), folder).exists()) {
                new File(getDataFolder(), folder).mkdirs();
            }
        }
        if (!new File(getDataFolder(), "config.json").exists()) {
            saveResource("config.json", false);
            ResourcesExtractor.extractResources(LUA_STATE_FOLDER, new File(getDataFolder(), LUA_STATE_FOLDER));
        }

        Config config;
        try {
            config = loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // load native library.
        getScheduler().runTaskAsynchronously(this, () -> {
            CompletableFuture<Object> future = new CompletableFuture<>();
            getScheduler().runTaskAsynchronously(this, () -> {
                asyncInit(config);
                future.complete(null);
            });

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
            try {
                future.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                DebugLogger.debug(DebugLogger.ERROR,
                        "Failed to waiting 'asyncInit' method complete.");
                DebugLogger.debug(e);
            }
            getScheduler().runTask(this, () -> init(config));
        });
    }

    /**
     * ahead of init().
     */
    private void asyncInit(Config config) {
        // set reflection util for luajava
        if (config.getLuaReflection() != null) {
            ReflectUtil reflectUtil = config.getLuaReflection().toReflectUtil();
            if (reflectUtil != null) {
                LuaJavaAPI.setReflectUtil(reflectUtil);
                DebugLogger.debug("Current using reflection util is %s", reflectUtil);
            }
        }

        // extract resources
        getLogger().info("Extract resources: " + LUA_LIB_FOLDER);
        ResourcesExtractor.extractResources(LUA_LIB_FOLDER, new File(getDataFolder(), LUA_LIB_FOLDER));

        // debug logger
        if (config.isDebug()) {
            try {
                DebugLogger.closeLogger();
                DebugLogger.init(getLogger(), new File(getDataFolder(), "debug.log"), 0xFFFF);
                getLogger().info("Successfully initialized debug log");
            } catch (IOException e) {
                getLogger().warning("Could not init debug log!");
            }
        }

        // bState
        if (config.isBStats()) {
            metrics = BStatsMetrics.newInstance(BSTATS_CODE);
        }

        // update checker
        if (config.isCheckUpdates()) {
            getScheduler().runTaskAsynchronously(this, () -> new UpdateChecker().checkForUpdates(logger()));
        }
    }

    /**
     * init will call after asyncInit method.
     */
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

    public static Logger logger() {
        return plugin.getLogger();
    }

    public File getLuaStateFolder() {
        return new File(getDataFolder(), LUA_STATE_FOLDER);
    }

    public ClassLoader classLoader() {
        return getClassLoader();
    }

    public String version() {
        String version = getDescription().getVersion();
        int i = version.indexOf('+');
        return i == -1 ? version : version.substring(0, i);
    }
}
