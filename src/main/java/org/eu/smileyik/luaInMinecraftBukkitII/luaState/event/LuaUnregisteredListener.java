package org.eu.smileyik.luaInMinecraftBukkitII.luaState.event;

import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.LuaStateEnv;

public class LuaUnregisteredListener {
    private final LuaStateEnv luaStateEnv;
    private final Listener listener;

    public LuaUnregisteredListener(LuaStateEnv luaStateEnv, Listener listener) {
        this.luaStateEnv = luaStateEnv;
        this.listener = listener;
    }

    public void register(String eventName) {
        this.luaStateEnv.registerEventListener(eventName, listener);
    }
}
