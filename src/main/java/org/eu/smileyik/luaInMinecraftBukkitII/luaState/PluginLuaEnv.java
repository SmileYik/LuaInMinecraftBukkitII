package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.LuaHelper;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.LuaIOHelper;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStatePluginEnv;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.NotNull;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaStateFacade;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;

@Getter
public class PluginLuaEnv implements ILuaStateEnvInner, ILuaStateEnv, ILuaStatePluginEnv {
    private final boolean ignoreAccessLimit;
    private LuaStateFacade lua;

    public PluginLuaEnv(boolean ignoreAccessLimit) {
        this.ignoreAccessLimit = ignoreAccessLimit;
    }

    @Override
    public void createEnv() {
        if (this.lua != null && !this.lua.isClosed()) {
            return;
        }

        this.lua = LuaStateFactory.newLuaState(!ignoreAccessLimit);
        String luaLibrary = new File(
                LuaInMinecraftBukkit.instance().getLuaStateFolder(), LuaInMinecraftBukkit.LUA_LIB_FOLDER)
                .getAbsolutePath();
        lua.openLibs();
        lua.getGlobal("package", LuaTable.class)
                .mapResultValue(table -> {
                    return table.get("path")
                            .mapValue(path -> (path) +
                                    ";" + luaLibrary + "/?.lua"
                            )
                            .mapResultValue(path -> table.put("path", path));
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
                    return table.put("helper", LuaHelper.class)
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

    @Override
    public void initialization() {

    }

    @Override
    public void checkScriptFilesUpdate() {

    }

    @Override
    public synchronized void close() {
        if (lua != null) {
            callClosure("onClose");
            lua.close();
            lua = null;
        }
    }

    @Override
    public synchronized void reload() {
        close();
        createEnv();
    }

    @Override
    public void softReload() {

    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public Result<Integer, LuaException> evalFile(String file) {
        return lua.evalFile(file)
                .ifFailureThen(err -> {
                    LuaInMinecraftBukkit.instance()
                            .getLogger()
                            .warning(String.format(
                                    "Failed to eval lua file '%s', because: %s", file, err.getMessage()));
                    DebugLogger.debug(DebugLogger.ERROR, err);
                });
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
}
