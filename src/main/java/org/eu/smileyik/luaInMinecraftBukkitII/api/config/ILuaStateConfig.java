package org.eu.smileyik.luaInMinecraftBukkitII.api.config;

import java.util.Map;

public interface ILuaStateConfig {
    public String getRootDir();
    public boolean isIgnoreAccessLimit();
    public Map<String, Object> getAttributes();
}
