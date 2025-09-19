package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import org.eu.smileyik.luajava.reflect.IExecutable;
import org.eu.smileyik.luajava.reflect.IFieldAccessor;
import org.eu.smileyik.luajava.reflect.LuaInvokedMethod;
import org.eu.smileyik.luajava.reflect.SimpleReflectUtil;
import org.eu.smileyik.luajava.util.LRUCache;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

public abstract class AbstractFastReflection extends SimpleReflectUtil {

    private final Map<Field, IFieldAccessor> fieldAccessors;
    private final Map<Constructor<?>, IExecutable<Constructor<?>>> constructAccessors;
    private final Map<Method, IExecutable<Method>> methodAccessors;
    private final Map<Method, IExecutable<Method>> lambdaMethodAccessors;

    public AbstractFastReflection(int cacheCapacity) {
        super(cacheCapacity);
        this.fieldAccessors = Collections.synchronizedMap(new LRUCache<>(cacheCapacity));
        this.constructAccessors = Collections.synchronizedMap(new LRUCache<>(cacheCapacity));
        this.methodAccessors = Collections.synchronizedMap(new LRUCache<>(cacheCapacity));
        this.lambdaMethodAccessors = Collections.synchronizedMap(new LRUCache<>(cacheCapacity));
    }

    @Override
    public IFieldAccessor findFieldByName(Class<?> clazz, String name, boolean ignoreFinal, boolean ignoreStatic, boolean ignoreNotStatic, boolean ignoreNotPublic) {
        IFieldAccessor accessor = super.findFieldByName(clazz, name, ignoreFinal, ignoreStatic, ignoreNotStatic, ignoreNotPublic);
        if (accessor == null) {
            return null;
        }
        return fieldAccessors.computeIfAbsent(accessor.getField(), field -> {
            DebugLogger.debug("Generating field accessor for %s", field);
            try {
                return newFieldAccessorWrapper(accessor);
            } catch (Throwable e) {
                DebugLogger.debug("Failed to init field accessors for field %s: %s", field, e.getMessage());
                DebugLogger.debug(e);
                return accessor;
            }
        });
    }

    @Override
    public LuaInvokedMethod<IExecutable<Constructor<?>>> findConstructorByParams(Class<?> clazz, Object[] params, boolean ignoreNotPublic, boolean ignoreStatic, boolean ignoreNotStatic) {
        LuaInvokedMethod<IExecutable<Constructor<?>>> result = super.findConstructorByParams(clazz, params, ignoreNotPublic, ignoreStatic, ignoreNotStatic);
        if (result == null) {
            return null;
        }
        IExecutable<Constructor<?>> executable = result.getExecutable();
        IExecutable<Constructor<?>> wrapper = constructAccessors.computeIfAbsent(executable.getExecutable(), constructor -> {
            DebugLogger.debug("Generating constructor accessor for %s", constructor);
            try {
                return newExecutableAccessorWrapper(executable);
            } catch (Throwable e) {
                DebugLogger.debug("Failed to init constructor accessors for constructor %s: %s", constructor, e.getMessage());
                DebugLogger.debug(e);
                return executable;
            }
        });
        return new LuaInvokedMethod<>(wrapper, result);
    }

    @Override
    public LinkedList<LuaInvokedMethod<IExecutable<Method>>> findMethodByParams(Class<?> clazz, String methodName, Object[] params, boolean justFirst, boolean ignoreNotPublic, boolean ignoreStatic, boolean ignoreNotStatic) {
        LinkedList<LuaInvokedMethod<IExecutable<Method>>> list = super.findMethodByParams(clazz, methodName, params, justFirst, ignoreNotPublic, ignoreStatic, ignoreNotStatic);
        if (list != null && list.size() == 1) {
            LuaInvokedMethod<IExecutable<Method>> result = list.removeFirst();
            IExecutable<Method> executable = result.getExecutable();
            IExecutable<Method> wrapper = null;
            Method targetMethod = executable.getExecutable();
            Method realMethod = ReflectUtil.getLambdaRealMethod(targetMethod);
            if (realMethod != null) {
                wrapper = lambdaMethodAccessors.computeIfAbsent(realMethod, method -> {
                    DebugLogger.debug("Generating lambda method accessor for %s", method);
                    try {
                        return newExecutableAccessorWrapper(executable);
                    } catch (Throwable e) {
                        DebugLogger.debug("Failed to init lambda method accessors for method %s: %s", realMethod, e.getMessage());
                        DebugLogger.debug(e);
                        return executable;
                    }
                });
            } else {
                wrapper = methodAccessors.computeIfAbsent(targetMethod, method -> {
                    DebugLogger.debug("Generating method accessor for %s", targetMethod);
                    try {
                        return newExecutableAccessorWrapper(executable);
                    } catch (Throwable e) {
                        DebugLogger.debug("Failed to init method accessors for method %s: %s", targetMethod, e.getMessage());
                        DebugLogger.debug(e);
                        return executable;
                    }
                });
            }
            list.add(new LuaInvokedMethod<>(wrapper, result));
        }
        return list;
    }

    protected abstract <T extends Executable> IExecutable<T> newExecutableAccessorWrapper(IExecutable<T> executable) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    protected abstract IFieldAccessor newFieldAccessorWrapper(IFieldAccessor field) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;
}
