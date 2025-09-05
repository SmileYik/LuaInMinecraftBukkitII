package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool;

import lombok.Getter;
import org.eu.smileyik.luajava.LuaStateFacade;

@Getter
public class LuaPoolEntity {
    private final LuaStateFacade luaStateFacade;
    private long totalMilliseconds = 0;
    private long latestRun = 0;
    private int runCount = 0;

    public LuaPoolEntity(LuaStateFacade luaStateFacade) {
        this.luaStateFacade = luaStateFacade;
    }

    public LuaStateFacade getLuaStateFacade() {
        latestRun = System.currentTimeMillis();
        runCount += 1;
        return luaStateFacade;
    }

    public void returned() {
        totalMilliseconds += System.currentTimeMillis() - latestRun;
    }

    public void close() {
        luaStateFacade.close();
    }

    public int getStateId() {
        return luaStateFacade.getStateId();
    }
}
