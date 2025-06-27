package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LuaStateConfig {
    private String rootDir;
    private boolean ignoreAccessLimit;
    private LuaInitConfig[] initialization;
}
