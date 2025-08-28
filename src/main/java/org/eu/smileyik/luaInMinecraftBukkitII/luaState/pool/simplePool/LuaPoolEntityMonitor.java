package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool;

import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.debug.LuaDebug;

import java.util.function.BiConsumer;

public class LuaPoolEntityMonitor implements BiConsumer<LuaStateFacade, LuaDebug> {
    private volatile boolean interrupted = false;
    private volatile String message = "interrupted";

    /**
     * Performs this operation on the given arguments.
     *
     * @param luaStateFacade the first input argument
     * @param luaDebug       the second input argument
     */
    @Override
    public void accept(LuaStateFacade luaStateFacade, LuaDebug luaDebug) {
        if (interrupted) {
            throw new RuntimeException(message);
        }
    }

    public void interrupt() {
        interrupted = true;
    }

    public void interrupt(String message) {
        this.message = message;
        this.interrupted = true;
    }

    public void initialization(LuaStateFacade luaStateFacade) {
        luaStateFacade.setDebugHook(this);
        luaStateFacade.getLuaState().setHook(
                LuaState.LUA_MASKCOUNT | LuaState.LUA_MASKCALL | LuaState.LUA_MASKRET, 100);
    }

    public void reset() {
        interrupted = false;
    }
}
