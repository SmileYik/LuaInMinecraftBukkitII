package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LuaInitConfig {
    /**
     * lua script file name
     */
    private String file;

    /**
     * auto reload lua script file when it changed
     */
    private boolean autoReload = false;

    /**
     * load lua script in async thread.
     */
    private boolean asyncLoad = false;

    /**
     * other bukkit plugin depends
     */
    private String[] depends;
}
