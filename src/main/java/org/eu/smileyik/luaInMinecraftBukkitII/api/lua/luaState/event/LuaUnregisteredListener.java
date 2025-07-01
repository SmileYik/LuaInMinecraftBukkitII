package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event;

import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.ILuaEnv;

/**
 * 未监听的事件类型.
 */
public class LuaUnregisteredListener {
    private final ILuaEnv luaEnv;
    private final Listener listener;

    public LuaUnregisteredListener(ILuaEnv luaEnv, Listener listener) {
        this.luaEnv = luaEnv;
        this.listener = listener;
    }

    /**
     * 注册Bukkit事件, 需要提供事件监听器名, 并且需要确保在同一个LuaState环境中,
     * 所有注册的事件的事件监听器名都必须唯一(没有重复的事件监听器名). 并且后续可以通过
     * 事件监听器名去取消监听已注册的事件.
     * @param eventName 事件监听器名.
     */
    public void register(String eventName) {
        this.luaEnv.registerEventListener(eventName, listener);
    }
}
