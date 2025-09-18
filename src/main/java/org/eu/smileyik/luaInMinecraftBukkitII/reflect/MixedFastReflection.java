package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection.*;
import org.eu.smileyik.luajava.reflect.IExecutable;
import org.eu.smileyik.luajava.reflect.IFieldAccessor;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.IOException;
import java.lang.reflect.*;

public class MixedFastReflection extends AbstractFastReflection {

    public MixedFastReflection(int cacheCapacity) {
        super(cacheCapacity);
    }

    @Override
    protected <T extends Executable> IExecutable<T> newExecutableAccessorWrapper(IExecutable<T> executable) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return new FastExecutorAccessor<>(executable);
    }

    @Override
    protected IFieldAccessor newFieldAccessorWrapper(IFieldAccessor field) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return new FastFieldAccessor(field);
    }

    private static class FastFieldAccessor implements IFieldAccessor {
        private final IFieldAccessor fallback;
        private final FieldAccessor accessor;
        public FastFieldAccessor(IFieldAccessor fallback) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.fallback = fallback;
            this.accessor = FieldGenerator.INSTANCE.generate(fallback.getField());
        }

        @Override
        public Object get(Object instance) {
            try {
                return accessor.get(instance);
            } catch (Exception e) {
                DebugLogger.debug(e);
                try {
                    return fallback.get(instance);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public void set(Object instance, Object value) {
            try {
                accessor.set(instance, value);
            } catch (Exception e) {
                DebugLogger.debug(e);
                try {
                    fallback.set(instance, value);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public Field getField() {
            return fallback.getField();
        }
    }

    private static class FastExecutorAccessor<T extends Executable> implements IExecutable<T> {
        private final IExecutable<T> fallback;
        private final ExecutorAccessor accessor;
        public FastExecutorAccessor(IExecutable<T> executable) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.fallback = executable;
            T t = executable.getExecutable();
            if (t instanceof Method) {
                this.accessor = MethodGenerator.INSTANCE.generate((Method) t);
            } else {
                this.accessor = ConstructorGenerator.INSTANCE.generate((Constructor<?>) t);
            }
        }

        @Override
        public Object invoke(Object instance, Object[] params) {
            try {
                return accessor.invoke(instance, params);
            } catch (Exception e) {
                DebugLogger.debug("Failed to invoke method %s", fallback.getExecutable().getName());
                DebugLogger.debug(e);
                try {
                    return fallback.invoke(instance, params);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public T getExecutable() {
            return fallback.getExecutable();
        }
    }
}
