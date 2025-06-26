package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.eu.smileyik.luajava.exception.Result;
import org.jetbrains.annotations.NotNull;

public interface ILuaStateEnv extends AutoCloseable {

    void initialization();

    void evalFile(String file);

    void evalLua(@NotNull String luaScript);

    Result<Object, Exception> callClosure(String globalClosureName, Object... params);

    void close();

    void reload();
}
