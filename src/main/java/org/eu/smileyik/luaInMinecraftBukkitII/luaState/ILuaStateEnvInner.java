package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luajava.LuaStateFacade;

public interface ILuaStateEnvInner extends ILuaStateEnv, AutoCloseable {
    void createEnv();

    LuaStateFacade createLuaState();

    void initialization();

    void checkScriptFilesUpdate();

    /**
     * 关闭 Lua 环境.
     */
    void close();

    /**
     * 重载Lua环境, 将会直接关闭现有Lua虚拟机.
     */
    void reload();

    /**
     * 软重载Lua环境.
     */
    void softReload();
}
