package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool;

import lombok.Getter;
import org.eu.smileyik.luajava.LuaStateFacade;

@Getter
public class LuaPoolEntity {
    private final LuaStateFacade luaStateFacade;
    private final LuaPoolEntityMonitor monitor;
    private long latestRun = 0;
    private int runCount = 0;

    public LuaPoolEntity(LuaStateFacade luaStateFacade) {
        this.luaStateFacade = luaStateFacade;
        this.monitor = new LuaPoolEntityMonitor();
        this.monitor.initialization(luaStateFacade);
    }

    public LuaStateFacade getLuaStateFacade() {
        latestRun = System.currentTimeMillis();
        runCount += 1;
        monitor.reset();
        return luaStateFacade;
    }

    public void close() {
        luaStateFacade.close();
    }

    public int getStateId() {
        return luaStateFacade.getStateId();
    }
}
