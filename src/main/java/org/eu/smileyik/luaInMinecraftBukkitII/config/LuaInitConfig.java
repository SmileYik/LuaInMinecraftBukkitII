package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LuaInitConfig {
    private String file;
    private boolean autoReload;
    private String[] depends;
}
