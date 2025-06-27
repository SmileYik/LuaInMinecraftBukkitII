package org.eu.smileyik.luaInMinecraftBukkitII.api;

import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.jetbrains.annotations.Nullable;

public interface ILuaStateManager extends AutoCloseable {
    @Nullable
    ILuaStateEnv getEnv(String id);

    void close();

    void reloadEnvScript(String id);

    void reload(Config config);
}
