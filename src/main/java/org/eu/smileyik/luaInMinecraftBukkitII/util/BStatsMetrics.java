package org.eu.smileyik.luaInMinecraftBukkitII.util;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.LuaStateEnv;
import org.eu.smileyik.luajava.LuaJavaAPI;
import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.reflect.ReflectUtil;
import org.eu.smileyik.luajava.reflect.SimpleReflectUtil;

import java.util.HashMap;
import java.util.Map;

public class BStatsMetrics {

    private static final DrilldownPie LUA_VERSION = new DrilldownPie("lua_version", () -> {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        String str = System.getProperty("os.name") + "-" +
                System.getProperty("os.version") + "-" +
                System.getProperty("os.arch");
        Map<String, Integer> entry = new HashMap<>();
        entry.put(str, 1);
        map.put(LuaState.LUA_VERSION, entry);
        return map;
    });

    private static final SimplePie LUA_REFLECTION_TYPE = new SimplePie("lua_reflection", () -> {
        Class<? extends ReflectUtil> aClass = LuaJavaAPI.getReflectUtil().getClass();
        if (aClass == SimpleReflectUtil.class) {
            return "Default";
        }
        return aClass.getSimpleName();
    });

    private static final SimplePie LUA_ENV_COUNT = new SimplePie("lua_environment_count", () ->
            String.valueOf(Integer.valueOf(LuaInMinecraftBukkit.instance().getLuaStateManager().getScriptEnvs().size())));

    private static final SimplePie LUA_SCRIPT_COUNT = new SimplePie("lua_script_count", () -> {
        int count = 0;
        for (ILuaStateEnv ienv : LuaInMinecraftBukkit.instance().getLuaStateManager().getScriptEnvs()) {
            if (ienv instanceof LuaStateEnv) {
                LuaStateEnv env = (LuaStateEnv) ienv;
                count += env.getConfig().getInitialization().length;
            }
        }
        return String.valueOf(count);
    });

    public static Metrics newInstance(int serviceId) {
        Metrics metrics = new Metrics(LuaInMinecraftBukkit.instance(), serviceId);
        metrics.addCustomChart(LUA_VERSION);
        metrics.addCustomChart(LUA_REFLECTION_TYPE);
        metrics.addCustomChart(LUA_ENV_COUNT);
        metrics.addCustomChart(LUA_SCRIPT_COUNT);
        return metrics;
    }
}
