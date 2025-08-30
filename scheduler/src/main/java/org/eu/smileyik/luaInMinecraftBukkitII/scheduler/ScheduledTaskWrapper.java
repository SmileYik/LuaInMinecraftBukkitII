package org.eu.smileyik.luaInMinecraftBukkitII.scheduler;

public interface ScheduledTaskWrapper<T> {

    public T getTask();

    public void cancel();
}
