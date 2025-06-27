package org.eu.smileyik.luaInMinecraftBukkitII.api.luaState;

import org.eu.smileyik.luajava.exception.Result;
import org.jetbrains.annotations.NotNull;
import org.keplerproject.luajava.LuaException;

public interface ILuaStateEnv {

    boolean isInitialized();

    Result<Integer, LuaException> evalFile(String file);

    void evalLua(@NotNull String luaScript);

    Result<Object, Exception> callClosure(String globalClosureName, Object... params);
}
