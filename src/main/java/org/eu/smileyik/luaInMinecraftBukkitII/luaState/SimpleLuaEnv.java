package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandClassBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandRegister;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListener;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListenerBuilder;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.simplecommand.CommandService;

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
    public LuaEventListenerBuilder listenerBuilder() {
        return new LuaEventListenerBuilder(this);
    }

    @Override
    public LuaCommandClassBuilder commandClassBuilder() {
        return new LuaCommandClassBuilder();
    }

    @Override
    public Result<Boolean, Exception> registerCommand(String rootCommand, Class<?>... classes) {
        try {
            CommandService commandService = LuaCommandRegister.register(rootCommand, classes);
            if (commandService == null) {
                return Result.success(false);
            }
            env.getCommandServices().put(rootCommand, commandService);
            commandService.registerToBukkit(LuaInMinecraftBukkit.instance());
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public void registerCleaner(ILuaCallable cleaner) {
        env.getCleaners().add(cleaner);
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
}
