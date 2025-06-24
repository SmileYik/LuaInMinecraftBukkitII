package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import java.lang.reflect.Field;

public class ReflectUtil {

    public static Field findFieldByType(Class<?> clazz, Class<?> targetType) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (targetType.isAssignableFrom(field.getType())) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
