package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AutoReloadConfig {
    /**
     * enable auto reload
     */
    private boolean enable = false;

    /**
     * blacklisted scripts
     */
    private String[] blacklist = new String[0];

    /**
     * check frequency, in milliseconds
     */
    private long frequency = 60 * 1000L;
}
