package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class Config {
    private String projectUrl = "https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/heads/gh-page";
    private String luaVersion = "lua-5.4.8";
    private Map<String, LuaStateConfig> luaState = new HashMap<>();
    private boolean alwaysCheckHashes = false;
    private boolean debug = false;
    private boolean bState = true;
}
