package org.eu.smileyik.luaInMinecraftBukkitII;

import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStatePluginEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.LuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.PluginLuaEnv;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LuaStateManager implements ILuaStateManager, Listener {
    @Getter
    private final Config config;
    private final Map<String, ILuaStateEnvInner> envs = new HashMap<>();
    private final Map<Plugin, ILuaStateEnvInner> pluginEnvs = new HashMap<>();

    public LuaStateManager(Config config) {
        this.config = config;
    }

    @Override
    public Collection<String> getScriptEnvIds() {
        return envs.keySet();
    }

    @Override
    public Collection<ILuaStateEnv> getScriptEnvs() {
        return Collections.unmodifiableCollection(envs.values());
    }

    @Override
    public ILuaStatePluginEnv createPluginEnv(@NotNull Plugin plugin) {
        return createPluginEnv(plugin, false);
    }

    @Override
    public ILuaStatePluginEnv createPluginEnv(@NotNull Plugin plugin, boolean ignoreAccessLimit) {
        if (plugin == LuaInMinecraftBukkit.instance()) {
            throw new IllegalArgumentException("Cannot create plugin env for LuaInMinecraftBukkit.");
        }
        destroyPluginEnv(plugin);
        ILuaStatePluginEnv env = new PluginLuaEnv(ignoreAccessLimit);
        ILuaStateEnvInner inner = (ILuaStateEnvInner) env;
        inner.createEnv();
        pluginEnvs.put(plugin, inner);
        return env;
    }

    @Override
    public ILuaStatePluginEnv getPluginEnv(@NotNull Plugin plugin) {
        return (ILuaStatePluginEnv) pluginEnvs.get(plugin);
    }

    @Override
    public void destroyPluginEnv(@NotNull Plugin plugin) {
        ILuaStateEnvInner removed = pluginEnvs.remove(plugin);
        if (removed != null) {
            removed.close();
        }
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
        pluginEnvs.values().forEach(ILuaStateEnvInner::close);
        pluginEnvs.clear();
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
        preLoad();
        initialization();
    }

    @Override
    public void preLoad() {
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
            envs.put(id, env);
        });
    }

    @Override
    public void initialization() {
        config.getLuaState().forEach((id, conf) -> {
            envs.get(id).initialization();
        });
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        envs.values().forEach(ILuaStateEnvInner::initialization);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        destroyPluginEnv(event.getPlugin());
    }
}
