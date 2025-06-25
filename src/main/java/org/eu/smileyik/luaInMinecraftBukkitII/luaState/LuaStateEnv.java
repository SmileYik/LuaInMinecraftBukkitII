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
import org.keplerproject.luajava.LuaStateFacade;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;
import java.util.*;

public class LuaStateEnv implements AutoCloseable {

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
    private final LuaStateFacade lua;
    @Getter
    private final File rootDir;

    @Getter(AccessLevel.PROTECTED)
    private final Map<String, Listener> listeners = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, CommandService> commandServices = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final List<ILuaCallable> cleaners = new LinkedList<>();
    private final ILuaEnv luaEnv = new SimpleLuaEnv(this);

    public LuaStateEnv(String id, LuaStateConfig config) {
        this.id = id;
        this.config = config;
        this.lua = LuaStateFactory.newLuaState(!this.config.isIgnoreAccessLimit());
        this.rootDir = new File(LuaInMinecraftBukkit.instance().getLuaStateFolder(), this.config.getRootDir());
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            DebugLogger.debug("Cannot create new directory: %s", rootDir);
        }
    }

    public void initialization() {
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

        lua.newTable();
        lua.toJavaObject(-1)
                .mapResultValue(obj -> {
                    LuaTable table = ((LuaTable) obj).asTable();
                    return table.put("env", luaEnv)
                            .mapResultValue(it -> table.put("plugin", LuaInMinecraftBukkit.instance()))
                            .mapResultValue(it -> table.put("bukkit", Bukkit.class))
                            .mapResultValue(it -> table.put("server", LuaInMinecraftBukkit.instance().getServer()))
                            .mapResultValue(it -> table.put("log", LuaInMinecraftBukkit.instance().getLogger()))
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
        }
    }
}
