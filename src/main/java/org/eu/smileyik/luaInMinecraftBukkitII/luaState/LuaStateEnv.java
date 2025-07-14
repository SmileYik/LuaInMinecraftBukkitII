package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.NativeLoader;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.ILuaEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.LuaHelper;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.LuaIOHelper;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaInitConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaStateConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListener;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.NotNull;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaStateFacade;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;
import java.util.*;

public class LuaStateEnv implements AutoCloseable, ILuaStateEnv, ILuaStateEnvInner {
    private final ILuaEnv luaEnv = new SimpleLuaEnv(this);

    @Getter
    private final String id;
    @Getter
    private final LuaStateConfig config;
    private final long[] initFileLoadedTimestamps;
    private final Object[] initFileLoadedLock;
    @Getter
    private final File rootDir;

    @Getter(AccessLevel.PROTECTED)
    private final Map<String, Listener> listeners = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, CommandService> commandServices = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final List<ILuaCallable> cleaners = new LinkedList<>();

    private LuaStateFacade lua;
    @Getter(AccessLevel.PUBLIC)
    private boolean initialized = false;

    public LuaStateEnv(String id, LuaStateConfig config) {
        this.id = id;
        this.config = config;
        this.initFileLoadedTimestamps = new long[config.getInitialization().length];
        this.initFileLoadedLock = new Object[config.getInitialization().length];
        for (int i = 0; i < this.initFileLoadedLock.length; i++) {
            this.initFileLoadedLock[i] = new Object();
        }
        this.rootDir = new File(LuaInMinecraftBukkit.instance().getLuaStateFolder(), this.config.getRootDir());
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            DebugLogger.debug("Cannot create new directory: %s", rootDir);
        }
    }

    @Override
    public void createEnv() {
        if (this.lua != null && !this.lua.isClosed()) {
            return;
        }

        this.lua = LuaStateFactory.newLuaState(!this.config.isIgnoreAccessLimit());
        String luaLibrary = new File(
                LuaInMinecraftBukkit.instance().getLuaStateFolder(), LuaInMinecraftBukkit.LUA_LIB_FOLDER)
                .getAbsolutePath();
        lua.openLibs();
        lua.getGlobal("package", LuaTable.class)
                .mapResultValue(table -> {
                    return table.get("cpath")
                            .mapValue(path -> {
                                for (String fileType : NativeLoader.getDynamicFileType()) {
                                    path += ";" + rootDir.getAbsolutePath() + "/?" + fileType;
                                    path += ";" + luaLibrary + "/?" +  fileType;
                                }
                                return path;
                            })
                            .mapResultValue(cpath -> table.put("cpath", cpath))
                            .mapResultValue(it -> {
                                return table.get("path")
                                        .mapValue(path -> (path) +
                                                ";" + rootDir.getAbsolutePath() + "/?.lua"+
                                                ";" + luaLibrary + "/?.lua"
                                        )
                                        .mapResultValue(path -> table.put("path", path));
                            });
                })
                .ifFailureThen(it -> {
                    DebugLogger.debug(DebugLogger.WARN,
                            "Error initializing lua package path: %s", it.getMessage());
                    DebugLogger.debug(DebugLogger.ERROR, it);
                });

        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        lua.newTable();
        lua.toJavaObject(-1)
                .mapResultValue(obj -> {
                    LuaTable table = ((LuaTable) obj).asTable();
                    return table.put("env", luaEnv)
                            .mapResultValue(it -> table.put("helper", LuaHelper.class))
                            .mapResultValue(it -> table.put("io",     LuaIOHelper.class))
                            .mapResultValue(it -> table.put("bukkit", Bukkit.class))
                            .mapResultValue(it -> table.put("plugin", plugin))
                            .mapResultValue(it -> table.put("server", plugin.getServer()))
                            .mapResultValue(it -> table.put("log",    plugin.getLogger()))
                            .mapResultValue(it -> table.put("out",    System.out))
                            .mapResultValue(it -> Result.success(table));
                })
                .mapResultValue(table -> lua.setGlobal("luaBukkit", table))
                .ifFailureThen(err -> {
                    DebugLogger.debug(DebugLogger.WARN,
                            "Error initializing global variable 'luaBukkit': %s", err.getMessage());
                    DebugLogger.debug(DebugLogger.ERROR, err);
                });
    }

    public synchronized void initialization() {
        if (initialized) {
            return;
        }

        boolean initialized = true;
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (int i = 0; i < initFileLoadedTimestamps.length; i++) {
            if (initFileLoadedTimestamps[i] != 0) {
                continue;
            }
            LuaInitConfig luaInitConfig = config.getInitialization()[i];
            boolean flag = true;
            for (String pluginName : luaInitConfig.getDepends()) {
                if (!pluginManager.isPluginEnabled(pluginName)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                final int finalI = i;
                if (luaInitConfig.isAsyncLoad()) {
                    plugin.getServer().getScheduler().runTaskAsynchronously(
                            plugin, () -> evalInitFile(luaInitConfig, finalI)
                    );
                } else {
                    evalInitFile(luaInitConfig, finalI);
                }

            } else {
                initialized = false;
            }
        }
        this.initialized = initialized;
        if (initialized) {
            DebugLogger.debug("[Lua env %s] lua file all initialized", id);
        }
    }

    private void evalInitFile(LuaInitConfig luaInitConfig, int idx) {
        synchronized (initFileLoadedLock[idx]) {
            evalFile(luaInitConfig.getFile())
                    .ifSuccessThen(it -> {
                        initFileLoadedTimestamps[idx] = System.currentTimeMillis();
                        DebugLogger.debug("[Lua env %s] lua file initialized: %s", id, luaInitConfig.getFile());
                    });
        }
    }

    @Override
    public void checkScriptFilesUpdate() {
        for (int i = 0; i < initFileLoadedTimestamps.length; i++) {
            if (initFileLoadedTimestamps[i] == 0) {
                continue;
            }
            
            LuaInitConfig luaInitConfig = config.getInitialization()[i];
            File file = luaEnv.file(luaInitConfig.getFile());
            long lasted = file.lastModified();
            if (lasted > initFileLoadedTimestamps[i]) {
                // TODO RELOAD
                initFileLoadedTimestamps[i] = System.currentTimeMillis();
            }
        }
    }

    @Override
    public Result<Integer, LuaException> evalFile(String file) {
        File scripFile = new File(rootDir, file);
        String absolutePath = scripFile.toString();
        if (scripFile.exists()) {
            return lua.evalFile(absolutePath)
                    .ifFailureThen(err -> {
                        LuaInMinecraftBukkit.instance()
                                .getLogger()
                                .warning(String.format(
                                        "Failed to eval lua file '%s', because: %s", file, err.getMessage()));
                        DebugLogger.debug(DebugLogger.ERROR, err);
                    });
        } else {
            LuaInMinecraftBukkit.instance()
                    .getLogger()
                    .warning(String.format("Cannot find file: %s", file));
            return Result.success(-1);
        }
    }

    @Override
    public Result<Integer, LuaException> evalLua(@NotNull String luaScript) {
        return lua.evalString(luaScript)
                .ifFailureThen(err -> {
                    LuaInMinecraftBukkit.instance()
                            .getLogger()
                            .warning(String.format(
                                    "Failed to eval lua script '%s', because: %s", luaScript, err.getMessage()));
                    DebugLogger.debug(DebugLogger.ERROR, err);
                });
    }

    /**
     * 执行一个Lua闭包全局变量.
     * @param globalClosureName Lua闭包全局变量名称.
     * @param params 传递给闭包的参数.
     * @return 调用结果
     */
    @Override
    public Result<Object, Exception> callClosure(String globalClosureName, Object... params) {
        return lua.getGlobal(globalClosureName)
                .mapResultValue(it -> it instanceof ILuaCallable ?
                        Result.success((ILuaCallable) it) :
                        Result.failure(new ClassCastException(String.format(
                                "Cannot cast type '%s' to type: ILuaCallable",
                                it == null ? null : it.getClass()))))
                .mapResultValue(callable -> callable.call(params))
                .justCast();
    }

    @Override
    public void close() {
        for (ILuaCallable cleaner : cleaners) {
            cleaner.call();
        }
        cleaners.clear();

        listeners.forEach((name, listener) -> {
            HandlerList.unregisterAll(listener);
            if (listener instanceof LuaEventListener) {
                ((LuaEventListener) listener).clear();
            }
        });
        listeners.clear();

        commandServices.forEach((name, commandService) -> {
            commandService.shutdown();
        });
        commandServices.clear();

        if (lua != null) {
            lua.close();
            lua = null;
        }

        // clear initialized state
        initialized = false;
        Arrays.fill(initFileLoadedTimestamps, 0);
    }

    @Override
    public void reload() {
        close();
        createEnv();
        initialization();
    }
}
