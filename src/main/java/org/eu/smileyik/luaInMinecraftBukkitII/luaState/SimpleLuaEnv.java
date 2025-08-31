package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.ILuaEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.ILuaCommandClassBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event.ILuaEventListenerBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandClassBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandRegister;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListener;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListenerBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.PooledLuaCallable;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.File;

public class SimpleLuaEnv implements ILuaEnv {
    private final LuaStateEnv env;

    public SimpleLuaEnv(LuaStateEnv env) {
        this.env = env;
    }

    @Override
    public void registerEventListener(String name, Listener listener) {
        Listener oldOne = env.getListeners().put(name, listener);
        LuaInMinecraftBukkit.instance()
                .getServer()
                .getPluginManager()
                .registerEvents(listener, LuaInMinecraftBukkit.instance());
        if (oldOne != null) {
            HandlerList.unregisterAll(oldOne);
        }
    }

    @Override
    public void unregisterEventListener(String name) {
        Listener remove = env.getListeners().remove(name);
        if (remove != null) {
            HandlerList.unregisterAll(remove);
            if (remove instanceof LuaEventListener) {
                ((LuaEventListener) remove).clear();
            }
        }
    }

    @Override
    public ILuaEventListenerBuilder listenerBuilder() {
        return new LuaEventListenerBuilder(this);
    }

    @Override
    public ILuaCommandClassBuilder commandClassBuilder() {
        return new LuaCommandClassBuilder();
    }

    @Override
    public Result<Boolean, Exception> registerCommand(String rootCommand, Class<?>... classes) {
        return registerCommand(rootCommand, null, classes);
    }

    @Override
    public Result<Boolean, Exception> registerCommand(String rootCommand, String[] aliases, Class<?>... classes) {
        try {
            CommandService commandService = LuaCommandRegister.register(rootCommand, aliases, classes);
            CommandService oldOne = env.getCommandServices().put(rootCommand, commandService);
            if (oldOne != null) {
                oldOne.shutdown();
            }
            return Result.success(true);
        } catch (Exception e) {
            LuaInMinecraftBukkit.instance().getLogger().warning(
                    "Failed to register command " + rootCommand + ": " + e.getMessage()
            );
            DebugLogger.debug(e);
            return Result.failure(e);
        }
    }

    @Override
    public void registerCleaner(ILuaCallable cleaner) {
        env.getCleaners().add(cleaner);
    }

    @Override
    public Result<Void, String> registerSoftReload(ILuaCallable luaCallable) {
        if (luaCallable == null) {
            return Result.failure("lua callable is null");
        }
        env.registerSoftReload(luaCallable);
        return Result.success(null);
    }

    @Override
    public ILuaCallable pooledCallable(ILuaCallable callable) {
        if (env.getLuaPool() == null) {
            LuaInMinecraftBukkit.instance()
                    .getLogger()
                    .warning(String.format(
                            "Lua environment '%s' don't enable lua pool, will using current lua state to call this closure.",
                            env.getId()));
            return callable;
        }
        return PooledLuaCallable.of(callable, env.getLuaPool());
    }

    @Override
    public String path(String path) {
        return file(path).toString();
    }

    @Override
    public String path(String... paths) {
        return file(paths).toString();
    }

    @Override
    public File file(String path) {
        return new File(env.getRootDir(), path);
    }

    @Override
    public File file(String... paths) {
        return new File(env.getRootDir(), String.join(File.pathSeparator, paths));
    }

    @Override
    public void setJustUseFirstMethod(boolean flag) {
        env.setJustUseFirstMethod(flag);
    }

    @Override
    public Result<Object, LuaException> ignoreMultiResultRun(ILuaCallable callable) {
        return env.ignoreMultiResultRun(callable);
    }
}
