package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

public interface ExecutorAccessor extends MemberAccessor {
    public Object invoke(Object instance, Object... args);
}
