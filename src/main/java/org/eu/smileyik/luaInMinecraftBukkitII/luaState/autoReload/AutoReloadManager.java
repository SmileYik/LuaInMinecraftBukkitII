package org.eu.smileyik.luaInMinecraftBukkitII.luaState.autoReload;

import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.config.AutoReloadConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaInitConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaStateConfig;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoReloadManager implements Runnable {

    private final String envId;
    private final Map<File, Long> files;
    private final ScheduledExecutorService scheduler;

    public AutoReloadManager(String envId, File rootDir, LuaStateConfig luaStateConfig) {
        this.envId = envId;
        Map<File, Long> files = new HashMap<>();
        AutoReloadConfig autoReloadConfig = luaStateConfig.getAutoReload();
        LuaInitConfig[] initialization = luaStateConfig.getInitialization();
        for (LuaInitConfig luaInitConfig : initialization) {
            if (Arrays.stream(autoReloadConfig.getBlacklist())
                    .anyMatch(it -> Objects.equals(it, luaInitConfig.getFile()))) {
                continue;
            }
            File file = new File(rootDir, luaInitConfig.getFile());
            long lastModified = file.lastModified();
            if (lastModified <= 0) {
                lastModified = System.currentTimeMillis();
            }
            files.put(file, lastModified);
        }
        this.files = files;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this,
                autoReloadConfig.getFrequency(),
                autoReloadConfig.getFrequency(),
                TimeUnit.MILLISECONDS
        );
        LuaInMinecraftBukkit.logger().info(String.format("[LuaEnv %s] Enabled auto-reload.", envId));
    }

    public void shutdown() {
        LuaInMinecraftBukkit.logger().info(String.format("[LuaEnv %s] Shutdown auto-reload.", envId));
        if (this.scheduler != null) {
            this.scheduler.shutdown();
        }
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        boolean needReload = false;
        for (Map.Entry<File, Long> entry : files.entrySet()) {
            File file = entry.getKey();
            Long lastModified = entry.getValue();

            if (!file.exists()) {
                continue;
            }
            long l = file.lastModified();
            if (l <= 0) {
                return;
            } else if (l > lastModified) {
                needReload = true;
                files.put(file, l);
                LuaInMinecraftBukkit.logger().info(String.format(
                        "[LuaEnv %s] Detected script file modification: %s",
                        envId, file.getName()
                ));
            }
        }
        if (needReload) {
            LuaInMinecraftBukkit.logger().info(String.format(
                    "[LuaEnv %s] Some file be modified, reloading lua environment", envId));
            LuaInMinecraftBukkit.instance()
                    .getLuaStateManager()
                    .reloadEnvScript(envId);
        }
    }
}
