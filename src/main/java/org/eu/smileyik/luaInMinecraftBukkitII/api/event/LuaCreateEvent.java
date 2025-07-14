package org.eu.smileyik.luaInMinecraftBukkitII.api.event;

import lombok.Getter;
import org.eu.smileyik.luaInMinecraftBukkitII.api.config.ILuaStateConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;

@Getter
public class LuaCreateEvent extends LuaEvent {
    private final ILuaStateConfig config;
    private final ILuaStateEnv env;

    public LuaCreateEvent(String envName, ILuaStateConfig config, ILuaStateEnv env) {
        super(envName);
        this.config = config;
        this.env = env;
    }

    public LuaCreateEvent(boolean async, String envName, ILuaStateConfig config, ILuaStateEnv env) {
        super(async, envName);
        this.config = config;
        this.env = env;
    }
}
