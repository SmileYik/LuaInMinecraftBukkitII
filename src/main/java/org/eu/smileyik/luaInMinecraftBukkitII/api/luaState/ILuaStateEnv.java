package org.eu.smileyik.luaInMinecraftBukkitII.api.luaState;

import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.ILuacage;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.exception.Result;
import org.jetbrains.annotations.NotNull;

public interface ILuaStateEnv {

    boolean isInitialized();

    Result<Integer, LuaException> evalFile(String file);

    Result<Integer, LuaException> evalLua(@NotNull String luaScript);

    Result<Object, Exception> callClosure(String globalClosureName, Object... params);

    ILuacage getLuacage();
}
