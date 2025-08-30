package org.eu.smileyik.luaInMinecraftBukkitII.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements Scheduler {
    @Override
    public ScheduledTaskWrapper<?> runTaskAsynchronously(Plugin plugin, Runnable task) {
        return new FoliaScheduledTaskWrapper(
                Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run())
        );
    }

    @Override
    public ScheduledTaskWrapper<?> runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        return new FoliaScheduledTaskWrapper(
                Bukkit.getAsyncScheduler().runDelayed(
                        plugin,
                        t -> task.run(),
                        delay * 50L,
                        TimeUnit.MILLISECONDS
                )
        );
    }

    @Override
    public ScheduledTaskWrapper<?> runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        return new FoliaScheduledTaskWrapper(
                Bukkit.getAsyncScheduler().runAtFixedRate(
                        plugin,
                        t -> task.run(),
                        delay * 50L,
                        period * 50L,
                        TimeUnit.MILLISECONDS
                )
        );
    }

    @Override
    public ScheduledTaskWrapper<?> runTask(Plugin plugin, Runnable task) {
        return new FoliaScheduledTaskWrapper(
                Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run())
        );
    }

    @Override
    public ScheduledTaskWrapper<?> runTaskLater(Plugin plugin, Runnable task, long delay) {
        return new FoliaScheduledTaskWrapper(
                Bukkit.getGlobalRegionScheduler().runDelayed(
                        plugin,
                        t -> task.run(),
                        delay
                )
        );
    }

    @Override
    public ScheduledTaskWrapper<?> runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        return new FoliaScheduledTaskWrapper(
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                        plugin,
                        t -> task.run(),
                        delay,
                        period
                )
        );
    }
}
