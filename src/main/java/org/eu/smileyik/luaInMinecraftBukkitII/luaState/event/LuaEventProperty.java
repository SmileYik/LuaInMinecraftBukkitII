package org.eu.smileyik.luaInMinecraftBukkitII.luaState.event;

import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.luajava.type.ILuaCallable;

@Data
@ToString
public class LuaEventProperty {
    private String event;
    private String priority;
    private boolean ignoreCancelled = false;
    private ILuaCallable handler;
}
