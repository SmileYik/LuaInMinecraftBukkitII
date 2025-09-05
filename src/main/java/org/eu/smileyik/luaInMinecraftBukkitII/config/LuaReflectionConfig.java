package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import org.eu.smileyik.luajava.reflect.ReflectUtil;
import org.eu.smileyik.luajava.reflect.SimpleReflectUtil;
import org.eu.smileyik.simpledebug.DebugLogger;

@Data
public class LuaReflectionConfig {
    /**
     * Reflection util type.
     * Optionals:
     *     org.eu.smileyik.luaInMinecraftBukkitII.reflect.FastReflection
     *     null
     */
    private String type = SimpleReflectUtil.class.getName();

    /**
     * reflection cache capacity
     */
    private int cacheCapacity = 1024;

    public ReflectUtil toReflectUtil() {
        try {
            return (ReflectUtil) Class.forName(type).getDeclaredConstructor(int.class).newInstance(cacheCapacity);
        } catch (Exception e) {
            DebugLogger.debug("Failed initializing reflection util: {}", e.getMessage());
            DebugLogger.debug(e);
            return null;
        }
    }
}
