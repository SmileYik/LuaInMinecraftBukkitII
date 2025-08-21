package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool;

import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;

public interface LuaPool {

    /**
     * 将当前闭包提交至Lua池中的状态机进行运行.
     * @param luaCallable 原闭包(需要与有效的Lua状态机绑定)
     * @param nres 返回参数
     * @param params lua 闭包形参
     * @return 运行结果
     */
    public Result<Object[], LuaException> submit(ILuaCallable luaCallable, int nres, Object... params);
}
