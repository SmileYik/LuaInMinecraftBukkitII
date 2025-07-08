package org.eu.smileyik.luaInMinecraftBukkitII.api.luaState;

import org.eu.smileyik.luajava.exception.Result;
import org.jetbrains.annotations.NotNull;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaStateFacade;

public interface ILuaStatePluginEnv {
    /**
     * 是否忽略 Java 中的访问限制.
     */
    public boolean isIgnoreAccessLimit();

    /**
     * 获取 Lua 实例.
     */
    public LuaStateFacade getLua();

    /**
     * 执行 Lua 脚本.
     * @param luaScript Lua 脚本.
     * @return 执行结果.
     */
    Result<Integer, LuaException> evalLua(@NotNull String luaScript);

    /**
     * 执行全局 Lua 闭包变量.
     * @param globalClosureName 闭包名
     * @param params            参数
     * @return 执行结果.
     */
    Result<Object, Exception> callClosure(String globalClosureName, Object... params);
}
