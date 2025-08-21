package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool;

import org.eu.smileyik.luaInMinecraftBukkitII.luaState.LuaStateEnv;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.LuaObject;
import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个Lua池的简单实现, 定位是, 其他线程获取空闲Lua状态机, 本身不创建线程.
 */
public class SimpleLuaPool implements LuaPool {
    private final LuaStateEnv env;
    private final int maxPoolSize;
    private int currentPoolSize = 0;

    private final LinkedList<LuaStateFacade> free;
    private final Set<LuaStateFacade> busy;

    private final Lock poolLock = new ReentrantLock();
    private final Condition freeCondition = poolLock.newCondition();

    public SimpleLuaPool(LuaStateEnv env, int maxPoolSize) {
        this.env = env;
        this.maxPoolSize = maxPoolSize;
        this.free = new LinkedList<>();
        this.busy = new HashSet<>(maxPoolSize);
    }

    @Override
    public Result<Object[], LuaException> submit(ILuaCallable luaCallable, int _nres, Object... params) {
        LuaStateFacade srcF = luaCallable.getLuaState();
        LuaStateFacade destF = getLuaState();
        LuaState srcL = srcF.getLuaState();
        LuaState destL = destF.getLuaState();

        boolean callable = false;
        int top = destL.getTop();

        srcF.lock();
        try {
            destL.newGlobalTable();
            srcL.getGlobal("_G");
            destL.getGlobal("_G");
            srcL.copyTableIfNotExists(-1, destL);
            srcL.pop(1);
            destL.pop(1);

            luaCallable.push();
            if (srcL.copyValue(-1, destL)) {
                callable = true;
                for (Object param : params) {
                    if (param instanceof LuaObject) {
                        ((LuaObject) param).rawPush();
                        if (!srcL.copyValue(-1, destL)) {
                            destL.pushNil();
                        }
                    } else {
                        destF.rawPushObjectValue(param)
                                .ifFailureThen(e -> destL.pushNil());
                    }
                }
            }
        } finally {
            srcF.unlock();
        }

        destF.lock();
        try {
            if (callable) {
                return destF.doPcall(params.length, _nres, 0)
                        .mapResultValue(v -> {
                            int nres = _nres;
                            int currentTop = destL.getTop();
                            if (nres == LuaState.LUA_MULTRET) {
                                nres = currentTop - top;
                            }
                            if (currentTop - top < nres) {
                                return Result.failure(new LuaException("Invalid Number of Results .")).justCast();
                            }

                            Object[] res = new Object[nres];
                            for (int i = nres - 1; i >= 0; i--) {
                                Result<Object, ? extends LuaException> ret = destF.rawToJavaObject(-1);
                                if (ret.isError()) return ret.justCast();
                                res[i] = ret.getValue();
                                destL.pop(1);
                            }
                            return Result.success(res);
                        });
            }
        } finally {
            destL.setTop(0);
            destF.unlock();
            returnLuaState(destF);
        }
        return Result.success();
    }

    protected LuaStateFacade newLuaState() {
        return env.createLuaState();
    }

    protected LuaStateFacade getLuaState() {
        poolLock.lock();
        try {
            LuaStateFacade luaStateFacade = null;
            if (free.isEmpty()) {
                if (currentPoolSize < maxPoolSize) {
                    currentPoolSize += 1;
                    luaStateFacade = newLuaState();
                } else {
                    try {
                        freeCondition.await();
                    } catch (InterruptedException ignored) {
                    }
                }
            } else {
                luaStateFacade = free.removeFirst();
            }
            if (luaStateFacade != null) {
                busy.add(luaStateFacade);
            }
            return luaStateFacade;
        } finally {
            poolLock.unlock();
        }
    }

    protected void returnLuaState(LuaStateFacade luaState) {
        poolLock.lock();
        try {
            if (busy.remove(luaState)) {
                free.addLast(luaState);
                freeCondition.signal();
            }
        } finally {
            poolLock.unlock();
        }
    }

}
