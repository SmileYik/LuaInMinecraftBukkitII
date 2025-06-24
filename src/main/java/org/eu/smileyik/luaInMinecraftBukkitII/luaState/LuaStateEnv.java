package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaStateConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListener;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaStateFacade;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LuaStateEnv implements AutoCloseable {
    @Getter
    private final String id;
    @Getter
    private final LuaStateConfig config;
    private final LuaStateFacade lua;
    @Getter
    private final File rootDir;

    private final Map<String, Listener> listeners = new HashMap<>();

    public LuaStateEnv(String id, LuaStateConfig config) {
        this.id = id;
        this.config = config;
        this.lua = LuaStateFactory.newLuaState(!this.config.isIgnoreAccessLimit());
        this.rootDir = new File(LuaInMinecraftBukkit.instance().getDataFolder(), this.config.getRootDir());
        if (!rootDir.exists()) {
            rootDir.mkdirs();
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
                    LuaTable table = (LuaTable) obj;
                    Result<Void, ? extends LuaException> result = table.put("env", this)
                            .mapResultValue(it -> table.put("plugin", LuaInMinecraftBukkit.instance()))
                            .mapResultValue(it -> table.put("bukkit", Bukkit.class))
                            .mapResultValue(it -> table.put("server", LuaInMinecraftBukkit.instance().getServer()));
                    return result.isError() ? result.justCast() : Result.success(table);
                })
                .mapResultValue(table -> lua.setGlobal("luaBukkit", table))
                .ifFailureThen(err -> {
                    DebugLogger.debug(DebugLogger.WARN,
                            "Error initializing global variable 'luaBukkit': %s", err.getMessage());
                    DebugLogger.debug(DebugLogger.ERROR, err);
                });
    }

    public void registerEventListener(String name, Listener listener) {
        Listener oldOne = listeners.put(name, listener);
        if (oldOne != null) {
            HandlerList.unregisterAll(oldOne);
        }
    }

    public LuaEventBuilder eventBuilder() {
        return new LuaEventBuilder(this);
    }

    public String path(String path) {
        return file(path).toString();
    }

    public String path(String ... paths) {
        return file(paths).toString();
    }

    public File file(String path) {
        return new File(this.rootDir, path);
    }

    public File file(String ... paths) {
        return new File(rootDir, String.join(File.pathSeparator, paths));
    }

    @Override
    public void close() {
        listeners.forEach((name, listener) -> {
            HandlerList.unregisterAll(listener);
            if (listener instanceof LuaEventListener) {
                ((LuaEventListener) listener).clear();
            }
        });
        listeners.clear();

        if (lua != null) {
            lua.close();
        }
    }
}
