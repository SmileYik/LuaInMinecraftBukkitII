package org.eu.smileyik.luaInMinecraftBukkitII.api.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 这是 Lua 事件的基类
 */
@Getter
public class LuaEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String envName;

    public LuaEvent(String envName) {
        this.envName = envName;
    }

    public LuaEvent(boolean async, String envName) {
        super(async);
        this.envName = envName;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
