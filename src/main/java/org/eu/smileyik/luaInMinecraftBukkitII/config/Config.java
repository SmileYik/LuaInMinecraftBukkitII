package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class Config {
    private Map<String, LuaStateConfig> luaState;
}
