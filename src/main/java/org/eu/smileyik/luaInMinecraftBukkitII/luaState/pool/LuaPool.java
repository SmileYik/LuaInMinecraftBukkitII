package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool;

import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaPoolConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * LuaPool 不自己创建线程, 应该附着到其他线程上调用.
 */
public interface LuaPool extends AutoCloseable {

    public static LuaPool create(ILuaStateEnvInner env, LuaPoolConfig config)
            throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        if (env == null || config == null || config.getType() == null || config.getType().isEmpty()) {
            return null;
        }

        String type = config.getType();
        Class<?> targetClass = Class.forName(type);
        Constructor<?> declaredConstructor = targetClass.getDeclaredConstructor(ILuaStateEnvInner.class, LuaPoolConfig.class);
        Object o = declaredConstructor.newInstance(env, config);
        return (LuaPool) o;
    }

    /**
     * 将当前闭包提交至Lua池中的状态机进行运行.
     * @param luaCallable 原闭包(需要与有效的Lua状态机绑定)
     * @param nres 返回参数
     * @param params lua 闭包形参
     * @return 运行结果
     */
    public Result<Object[], LuaException> submit(ILuaCallable luaCallable, int nres, Object... params);

    void close();
}
