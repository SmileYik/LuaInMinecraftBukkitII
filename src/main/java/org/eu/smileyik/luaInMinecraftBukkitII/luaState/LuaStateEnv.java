package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaStateConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListener;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.NotNull;
import org.keplerproject.luajava.LuaStateFacade;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;
import java.util.*;

public class LuaStateEnv implements AutoCloseable, ILuaStateEnv {

    static {
        File folder = new File(
                LuaInMinecraftBukkit.instance().getDataFolder(),
                LuaInMinecraftBukkit.NATIVES_FOLDER);
        if (!folder.exists()) {
            throw new UnsupportedOperationException("Not found natives libraries");
        }
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            System.load(file.getAbsolutePath());
        }
    }

    @Getter
    private final String id;
    @Getter
    private final LuaStateConfig config;
    @Getter
    private final File rootDir;

    @Getter(AccessLevel.PROTECTED)
    private final Map<String, Listener> listeners = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, CommandService> commandServices = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final List<ILuaCallable> cleaners = new LinkedList<>();
    private final ILuaEnv luaEnv = new SimpleLuaEnv(this);

    private LuaStateFacade lua;

    public LuaStateEnv(String id, LuaStateConfig config) {
        this.id = id;
        this.config = config;
        this.rootDir = new File(LuaInMinecraftBukkit.instance().getLuaStateFolder(), this.config.getRootDir());
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            DebugLogger.debug("Cannot create new directory: %s", rootDir);
        }
    }

    @Override
    public void initialization() {
        if (this.lua != null && !this.lua.isClosed()) {
            return;
        }

        this.lua = LuaStateFactory.newLuaState(!this.config.isIgnoreAccessLimit());
        lua.openLibs();
        lua.getGlobal("package", LuaTable.class)
                .mapResultValue(table -> {
                    return table.get("cpath")
                            .mapValue(path -> (path) +
                                    ";" + rootDir.getAbsolutePath() + "/?.so" +
                                    ";" + rootDir.getAbsoluteFile() + "/?.dll"
                            )
                            .mapResultValue(cpath -> table.put("cpath", cpath))
                            .mapResultValue(it -> {
                                return table.get("path")
                                        .mapValue(path -> (path) +
                                                ";" + rootDir.getAbsolutePath() + "/?.lua"
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
                            .mapResultValue(it -> table.put("bukkit", Bukkit.class))
                            .mapResultValue(it -> table.put("plugin", plugin))
                            .mapResultValue(it -> table.put("server", plugin.getServer()))
                            .mapResultValue(it -> table.put("log",    plugin.getLogger()))
                            .mapResultValue(it -> Result.success(table));
                })
                .mapResultValue(table -> lua.setGlobal("luaBukkit", table))
                .ifFailureThen(err -> {
                    DebugLogger.debug(DebugLogger.WARN,
                            "Error initializing global variable 'luaBukkit': %s", err.getMessage());
                    DebugLogger.debug(DebugLogger.ERROR, err);
                });

        for (String file : config.getInitialization()) {
            evalFile(file);
        }
    }

    @Override
    public void evalFile(String file) {
        File scripFile = new File(rootDir, file);
        String absolutePath = scripFile.toString();
        if (scripFile.exists()) {
            lua.evalFile(absolutePath)
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
        }
    }

    @Override
    public void evalLua(@NotNull String luaScript) {
        lua.evalString(luaScript)
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
    }

    @Override
    public void reload() {
        close();
        initialization();
    }
}
