package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LuaPoolConfig {
    /**
     * Enable lua pool
     */
    private boolean enable;

    /**
     * lua pool type
     */
    private String type = "org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool.SimpleLuaPool";

    /**
     * max size of lua pool.
     */
    private int maxSize = 4;

    /**
     * max idle lua state not be close.
     */
    private int idleSize = 1;

    /**
     * close the lua state if it idled long time. (milliseconds)
     */
    private long idleTimeout = 10 * 60 * 1000;
}
