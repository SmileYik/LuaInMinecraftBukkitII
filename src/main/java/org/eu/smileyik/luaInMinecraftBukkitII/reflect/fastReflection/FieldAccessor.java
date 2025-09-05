package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

public interface FieldAccessor extends MemberAccessor {
    public Object get(Object instance);
    public void set(Object instance, Object value);
}
