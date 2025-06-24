package org.eu.smileyik.luaInMinecraftBukkitII;

import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.LuaStateEnv;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LuaInMinecraftBukkit extends JavaPlugin {
    public static final String NATIVES_FOLDER = "natives";
    public static final String LUA_STATE_FOLDER = "luastate";

    private static final String[] FOLDERS = new String[] {
            LUA_STATE_FOLDER,
            NATIVES_FOLDER,
            "scripts"
    };

    private static LuaInMinecraftBukkit plugin;
    private final Map<String, LuaStateEnv> envs = new HashMap<>();

    public LuaInMinecraftBukkit() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        for (String folder : FOLDERS) {
            if (!new File(getDataFolder(), folder).exists()) {
                new File(getDataFolder(), folder).mkdirs();
            }
        }
        if (!new File(getDataFolder(), "config.json").exists()) {
            saveResource("config.json", false);
        }
        try {
            DebugLogger.init(getLogger(), new File(getDataFolder(), "debug.log"), 0xFFFF);
            List<String> strings = Files.readAllLines(new File(getDataFolder(), "config.json").toPath());
            String json = String.join("\n", strings);
            json = JsonUtil.stripComments(json);
            Config config1 = new Gson().fromJson(json, Config.class);
            config1.getLuaState().forEach((id, config) -> {
                LuaStateEnv env = new LuaStateEnv(id, config);
                env.initialization();
                envs.put(id, env);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        envs.forEach((id, env) -> {
            env.close();
        });
        envs.clear();
        try {
            DebugLogger.closeLogger();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LuaInMinecraftBukkit instance() {
        return plugin;
    }

    public File getLuaStateFolder() {
        return new File(getDataFolder(), LUA_STATE_FOLDER);
    }
}
