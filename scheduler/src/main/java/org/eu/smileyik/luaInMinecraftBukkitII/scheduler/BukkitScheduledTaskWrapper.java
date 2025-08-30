package org.eu.smileyik.luaInMinecraftBukkitII.scheduler;

import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitScheduledTaskWrapper implements ScheduledTaskWrapper<BukkitTask> {

    @Getter
    private final BukkitTask task;

    public BukkitScheduledTaskWrapper(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}