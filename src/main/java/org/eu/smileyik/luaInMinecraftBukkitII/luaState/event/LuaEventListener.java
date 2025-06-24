package org.eu.smileyik.luaInMinecraftBukkitII.luaState.event;

import lombok.Getter;
import org.bukkit.event.Listener;

@Getter
public abstract class LuaEventListener implements Listener {
    private final LuaEventHandler eventHandler;

    public LuaEventListener(LuaEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void clear() {
        if (eventHandler != null) {
            eventHandler.clear();
        }
    }

}
