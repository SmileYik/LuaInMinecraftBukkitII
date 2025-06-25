package org.eu.smileyik.luaInMinecraftBukkitII.luaState.event;

import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaEnv;

public class LuaUnregisteredListener {
    private final ILuaEnv luaEnv;
    private final Listener listener;

    public LuaUnregisteredListener(ILuaEnv luaEnv, Listener listener) {
        this.luaEnv = luaEnv;
        this.listener = listener;
    }

    public void register(String eventName) {
        this.luaEnv.registerEventListener(eventName, listener);
    }
}
