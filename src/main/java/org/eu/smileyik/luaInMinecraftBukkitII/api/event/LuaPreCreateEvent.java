package org.eu.smileyik.luaInMinecraftBukkitII.api.event;

import lombok.Getter;
import org.eu.smileyik.luaInMinecraftBukkitII.api.config.ILuaStateConfig;

@Getter
public class LuaPreCreateEvent extends LuaEvent {
    private final ILuaStateConfig config;

    public LuaPreCreateEvent(String envName, ILuaStateConfig config) {
        super(envName);
        this.config = config;
    }

    public LuaPreCreateEvent(boolean async, String envName, ILuaStateConfig config) {
        super(async, envName);
        this.config = config;
    }
}
