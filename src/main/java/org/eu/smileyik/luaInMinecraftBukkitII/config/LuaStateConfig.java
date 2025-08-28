package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.luaInMinecraftBukkitII.api.config.ILuaStateConfig;

import java.util.Map;

@Data
@ToString
public class LuaStateConfig implements ILuaStateConfig {
    /** Root dir of lua env. */
    private String rootDir;

    /** force access java field and method or not */
    private boolean ignoreAccessLimit;

    /** lua pool config */
    private LuaPoolConfig pool;
    private LuaInitConfig[] initialization;
    private Map<String, Object> attributes;
}
