package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class LuacageLuaMeta extends LuacageCommonMeta {

    /**
     * main lua file of
     */
    private String main;

    /**
     * depend lua packages
     */
    private String[] dependPackages;

    /**
     * depend bukkit plugin
     */
    private String[] dependPlugins;

    /**
     * is runnable.
     */
    private boolean runnable;

    /**
     * the reason if not runnable.
     */
    private String reason;
}
