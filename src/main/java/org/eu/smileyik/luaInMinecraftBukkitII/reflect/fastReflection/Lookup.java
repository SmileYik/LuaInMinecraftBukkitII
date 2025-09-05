package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Lookup {

    public static MethodHandle getMethodHandle(Method method) throws Exception {
        int modifiers = method.getModifiers();
        Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = Modifier.isPublic(modifiers)
                ? MethodHandles.lookup() : createPrivateLookupIn(declaringClass);
        return lookup.unreflect(method);
    }

    public static MethodHandle getMethodHandle(Constructor<?> constructor) throws Exception {
        int modifiers = constructor.getModifiers();
        Class<?> declaringClass = constructor.getDeclaringClass();
        MethodHandles.Lookup lookup = Modifier.isPublic(modifiers)
                ? MethodHandles.lookup() : createPrivateLookupIn(declaringClass);
        return lookup.unreflectConstructor(constructor);
    }

    public static MethodHandle getFieldSetter(Field field) throws Exception {
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            return null;
        }
        return lookup(Modifier.isPublic(modifiers), field.getDeclaringClass()).unreflectSetter(field);
    }

    public static MethodHandle getFieldGetter(Field field) throws Exception {
        int modifiers = field.getModifiers();
        return lookup(Modifier.isPublic(modifiers), field.getDeclaringClass()).unreflectGetter(field);
    }

    public static MethodHandles.Lookup lookup(boolean isPublic, Class<?> targetClass) throws Exception {
        return isPublic ? MethodHandles.lookup() : createPrivateLookupIn(targetClass);
    }

    public static MethodHandles.Lookup createPrivateLookupIn(Class<?> targetClass) throws Exception {
        try {
            return (MethodHandles.Lookup) MethodHandles.class
                    .getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class)
                    .invoke(null, targetClass, MethodHandles.lookup());
        } catch (NoSuchMethodException e) {
            // java 8
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            return constructor.newInstance(targetClass);
        }
    }

}
