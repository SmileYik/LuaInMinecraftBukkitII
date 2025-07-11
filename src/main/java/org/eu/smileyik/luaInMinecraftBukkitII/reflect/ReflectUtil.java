package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import org.eu.smileyik.luajava.reflect.LuaInvokedMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

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

    public static Object newInstance(Class<?> clazz, Object... _args)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz == null) {
            throw new NullPointerException("class is null");
        }
        if (_args == null) {
            _args = new Object[0];
        }
        Object[] args = _args;
        LuaInvokedMethod<Constructor<?>> constructor = org.eu.smileyik.luajava.reflect.ReflectUtil
                .findConstructorByParams(clazz, args, false, false, false);
        if (constructor == null) {
            throw new NullPointerException("No constructor found in " + clazz + " by params: " + Arrays.toString(args));
        }

        constructor.getOverwriteParams().forEach((idx, value) -> args[idx] = value);
        Constructor<?> executable = constructor.getExecutable();
        executable.setAccessible(true);
        return executable.newInstance(args);
    }

    public static Object callMethod(Object object, String methodName, Object... _args)
            throws InvocationTargetException, IllegalAccessException {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (_args == null) {
            _args = new Object[0];
        }
        Object[] args = _args;
        LinkedList<LuaInvokedMethod<Method>> methodByParams =
                org.eu.smileyik.luajava.reflect.ReflectUtil.findMethodByParams(
                        object.getClass(), methodName, args, false, false, false, false);
        if (methodByParams.isEmpty()) {
            throw new NullPointerException("No method found for " + methodName + " in " + object.getClass() + " by params: " + Arrays.toString(args));
        }
        if (methodByParams.size() != 1) {
            throw new NullPointerException("Multi-target method found for " + methodName + " in " + object.getClass() + " by params: " + Arrays.toString(args));
        }
        LuaInvokedMethod<Method> invokedMethod = methodByParams.removeFirst();
        invokedMethod.getOverwriteParams().forEach((idx, value) -> args[idx] = value);
        Method executable = invokedMethod.getExecutable();
        executable.setAccessible(true);
        return executable.invoke(object, args);
    }

    public static boolean hasMethod(Object object, String methodName) {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (methodName == null || methodName.isEmpty()) {
            throw new NullPointerException("methodName is null or empty");
        }
        return org.eu.smileyik.luajava.reflect.ReflectUtil.existsMethodByName(
                object.getClass(), methodName, false, false, false);
    }

    public static boolean hasField(Object object, String fieldName) {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new NullPointerException("fieldName is null or empty");
        }
        return org.eu.smileyik.luajava.reflect.ReflectUtil.findFieldByName(
                object.getClass(), fieldName, false, false, false, false) != null;
    }
}
