package org.eu.smileyik.luaInMinecraftBukkitII;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.LuaStateEnv;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LuaStateManager implements ILuaStateManager, Listener {
    private final Map<String, ILuaStateEnvInner> envs = new HashMap<>();

    public LuaStateManager(Config config) {
        reload(config);
    }

    @Override
    @Nullable
    public ILuaStateEnv getEnv(String id) {
        return envs.get(id);
    }

    @Override
    public void close() {
        envs.values().forEach(ILuaStateEnvInner::close);
        envs.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void reloadEnvScript(String id) {
        ILuaStateEnvInner env = envs.get(id);
        if (env != null) {
            env.reload();
        }
    }

    @Override
    public void reload(Config config) {
        close();

        LuaInMinecraftBukkit.instance()
                .getServer()
                .getPluginManager()
                .registerEvents(this, LuaInMinecraftBukkit.instance());
        config.getLuaState().forEach((id, conf) -> {
            ILuaStateEnvInner env = new LuaStateEnv(id, conf);
            try {
                env.createEnv();
            } catch (Exception e) {
                LuaInMinecraftBukkit.instance().getLogger()
                        .warning("Error while initializing lua state env '" + id + "': " + e.getMessage());
                DebugLogger.debug(e);
                return;
            }
            env.initialization();
            envs.put(id, env);
        });
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        envs.values().forEach(ILuaStateEnvInner::initialization);
    }
}
