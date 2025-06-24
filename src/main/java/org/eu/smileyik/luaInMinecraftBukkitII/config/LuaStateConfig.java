package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class LuaStateConfig {
    private String rootDir;
    private boolean ignoreAccessLimit;
    private List<String> initialization;
}
