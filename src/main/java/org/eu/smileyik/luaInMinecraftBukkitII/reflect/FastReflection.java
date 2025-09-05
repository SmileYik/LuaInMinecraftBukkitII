package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection.*;
import org.eu.smileyik.luajava.reflect.IExecutable;
import org.eu.smileyik.luajava.reflect.IFieldAccessor;
import org.eu.smileyik.luajava.reflect.LuaInvokedMethod;
import org.eu.smileyik.luajava.reflect.SimpleReflectUtil;
import org.eu.smileyik.luajava.util.LRUCache;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.LinkedList;

public class FastReflection extends SimpleReflectUtil {

    private final LRUCache<IFieldAccessor, IFieldAccessor> fieldAccessors;
    private final LRUCache<IExecutable<Constructor<?>>, IExecutable<Constructor<?>>> constructAccessors;
    private final LRUCache<IExecutable<Method>, IExecutable<Method>> methodAccessors;
    private final LRUCache<Method, IExecutable<Method>> lambdaMethodAccessors;

    public FastReflection(int cacheCapacity) {
        super(cacheCapacity);
        this.fieldAccessors = new LRUCache<>(cacheCapacity);
        this.constructAccessors = new LRUCache<>(cacheCapacity);
        this.methodAccessors = new LRUCache<>(cacheCapacity);
        this.lambdaMethodAccessors = new LRUCache<>(cacheCapacity);
    }

    @Override
    public IFieldAccessor findFieldByName(Class<?> clazz, String name, boolean ignoreFinal, boolean ignoreStatic, boolean ignoreNotStatic, boolean ignoreNotPublic) {
        IFieldAccessor accessor = super.findFieldByName(clazz, name, ignoreFinal, ignoreStatic, ignoreNotStatic, ignoreNotPublic);
        if (accessor == null) {
            return null;
        }
        return fieldAccessors.computeIfAbsent(accessor, field -> {
            try {
                return new FastFieldAccessor(field);
            } catch (Exception e) {
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

        IExecutable<Constructor<?>> wrapper = constructAccessors.computeIfAbsent(executable, constructor -> {
            try {
                return new FastExecutorAccessor<Constructor<?>>(constructor);
            } catch (Exception e) {
                return constructor;
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
            Method targetMethod = executable.getExecutable();
            IExecutable<Method> wrapper = null;
            if (ReflectUtil.isLambdaInstance(targetMethod.getDeclaringClass())) {
                targetMethod = ReflectUtil.getLambdaRealMethod(targetMethod);
                if (targetMethod != null) {
                    wrapper = lambdaMethodAccessors.computeIfAbsent(targetMethod, method -> {
                        try {
                            return new FastExecutorAccessor<Method>(executable);
                        } catch (Exception e) {
                            return executable;
                        }
                    });
                }
            } else {
                wrapper = methodAccessors.computeIfAbsent(executable, method -> {
                    try {
                        return new FastExecutorAccessor<Method>(method);
                    } catch (Exception e) {
                        return method;
                    }
                });
            }
            list.add(new LuaInvokedMethod<>(wrapper, result));
        }
        return list;
    }

    private static class FastFieldAccessor implements IFieldAccessor {
        private final Field field;
        private final FieldAccessor accessor;
        public FastFieldAccessor(IFieldAccessor fallback) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.field = fallback.getField();
            this.accessor = FieldGenerator.INSTANCE.generate(fallback.getField());
        }

        @Override
        public Object get(Object instance) {
            return accessor.get(instance);
        }

        @Override
        public void set(Object instance, Object value) {
            accessor.set(instance, value);
        }

        @Override
        public Field getField() {
            return field;
        }
    }

    private static class FastExecutorAccessor<T extends Executable> implements IExecutable<T> {
        private final T executable;
        private final ExecutorAccessor accessor;
        public FastExecutorAccessor(IExecutable<T> executable) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.executable = executable.getExecutable();
            if (this.executable instanceof Method) {
                this.accessor = MethodGenerator.INSTANCE.generate((Method) this.executable);
            } else {
                this.accessor = ConstructorGenerator.INSTANCE.generate((Constructor<?>) this.executable);
            }
        }

        @Override
        public Object invoke(Object instance, Object[] params) {
            return accessor.invoke(instance, params);
        }

        @Override
        public T getExecutable() {
            return executable;
        }
    }
}
