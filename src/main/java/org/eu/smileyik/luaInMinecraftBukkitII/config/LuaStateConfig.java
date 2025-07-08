package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.luaInMinecraftBukkitII.api.config.ILuaStateConfig;

import java.util.Map;

@Data
@ToString
public class LuaStateConfig implements ILuaStateConfig {
    private String rootDir;
    private boolean ignoreAccessLimit;
    private LuaInitConfig[] initialization;
    private Map<String, Object> attributes;
}
