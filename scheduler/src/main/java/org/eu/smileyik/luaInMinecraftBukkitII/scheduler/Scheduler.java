package org.eu.smileyik.luaInMinecraftBukkitII.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;

public interface Scheduler {
    public static final String FOLIA_SCHEDULER = "org.eu.smileyik.luaInMinecraftBukkitII.scheduler.FoliaScheduler";

    public static Scheduler newInstance() {

        // if Bukkit class include method "getAsyncScheduler" then return FoliaScheduler.
        try {
            for (Method declaredMethod : Bukkit.class.getDeclaredMethods()) {
                String name = declaredMethod.getName();
                if (Objects.equals("getAsyncScheduler", name)) {
                    Class<?> aClass = Class.forName(FOLIA_SCHEDULER);
                    return (Scheduler) aClass.getDeclaredConstructor().newInstance();
                }
            }
        } catch (Exception ignored) {

        }

        return new BukkitScheduler();
    }

    public default ScheduledTaskWrapper<?> runTaskAsynchronously(Plugin plugin, Runnable task) {
        return new BukkitScheduledTaskWrapper(
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task)
        );
    }

    public default ScheduledTaskWrapper<?> runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        return new BukkitScheduledTaskWrapper(
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay)
        );
    }

    public default ScheduledTaskWrapper<?> runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        return new BukkitScheduledTaskWrapper(
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period)
        );
    }

    public default ScheduledTaskWrapper<?> runTask(Plugin plugin, Runnable task) {
        return new BukkitScheduledTaskWrapper(
                Bukkit.getScheduler().runTask(plugin, task)
        );
    }

    public default ScheduledTaskWrapper<?> runTaskLater(Plugin plugin, Runnable task, long delay) {
        return new BukkitScheduledTaskWrapper(
                Bukkit.getScheduler().runTaskLater(plugin, task, delay)
        );
    }

    public default ScheduledTaskWrapper<?> runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        return new BukkitScheduledTaskWrapper(
                Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period)
        );
    }
}
