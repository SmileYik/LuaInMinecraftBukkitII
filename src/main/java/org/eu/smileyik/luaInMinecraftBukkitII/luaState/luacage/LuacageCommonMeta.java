package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class LuacageCommonMeta {
    /**
     * package name
     */
    private String name;

    /**
     * package version
     */
    private String version;

    /**
     * package authors
     */
    private String[] authors;

    /**
     * describe your package
     */
    private String description;

    /**
     * runnable lua version
     */
    private String[] luaVersion;

    /**
     * depend lua packages
     */
    private String[] dependPackages;
}
