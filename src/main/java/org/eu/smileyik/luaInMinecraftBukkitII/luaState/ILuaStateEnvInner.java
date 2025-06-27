package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;

public interface ILuaStateEnvInner extends ILuaStateEnv, AutoCloseable {
    void createEnv();

    void initialization();

    void checkScriptFilesUpdate();

    void close();

    void reload();
}
