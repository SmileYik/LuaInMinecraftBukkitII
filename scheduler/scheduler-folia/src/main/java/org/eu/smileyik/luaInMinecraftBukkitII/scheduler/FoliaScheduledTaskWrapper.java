package org.eu.smileyik.luaInMinecraftBukkitII.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;

public final class FoliaScheduledTaskWrapper implements ScheduledTaskWrapper<ScheduledTask> {

    @Getter
    private final ScheduledTask task;

    public FoliaScheduledTaskWrapper(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}