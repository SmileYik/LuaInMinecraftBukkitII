package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool;

import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaPoolConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.LuaPool;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.LuaObject;
import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.ILuaObject;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 目标: 懒创建Lua环境, 若长时间没有运行则清除Lua环境
 */
public class SimpleLuaPool implements LuaPool, AutoCloseable {
    private final ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
    private final ILuaStateEnvInner env;
    private final LuaPoolConfig config;

    private int currentPoolSize = 0;
    private final PriorityQueue<LuaPoolEntity> queue;
    private final Set<LuaPoolEntity> running;
    private final ExecutorService executor;

    private final Lock poolLock = new ReentrantLock();
    private final Condition freeCondition = poolLock.newCondition();

    public SimpleLuaPool(ILuaStateEnvInner env, LuaPoolConfig config) {
        this.env = env;
        this.config = config;
        this.queue = new PriorityQueue<>(this.config.getMaxSize(),
                Comparator.comparingLong(LuaPoolEntity::getLatestRun));
        this.executor = Executors.newFixedThreadPool(config.getMaxSize());
        this.running = new HashSet<>(this.config.getMaxSize());
        this.scheduled.scheduleAtFixedRate(() -> {
            long time = System.currentTimeMillis();
            poolLock.lock();
            try {
                while (!queue.isEmpty()) {
                    LuaPoolEntity peek = queue.peek();
                    if (time - peek.getLatestRun() >= config.getIdleTimeout()) {
                        queue.poll();
                        currentPoolSize -= 1;
                        DebugLogger.debug("Removing idle lua state, this lua state be called %d times, latest run at %d",
                                peek.getRunCount(), peek.getLatestRun());
                        peek.getLuaStateFacade().close();
                        continue;
                    }
                    break;
                }
            } finally {
                poolLock.unlock();
            }
        }, config.getIdleTimeout(), config.getIdleTimeout() >> 1, TimeUnit.MILLISECONDS);
    }

    private boolean transferClosure(
            LuaStateFacade srcF,
            LuaStateFacade destF,
            ILuaCallable callable,
            Object... params
    ) {
        LuaState srcL = srcF.getLuaState();
        LuaState destL = destF.getLuaState();

        // copy lua closure
        srcF.lock();
        try {
            callable.push();
            try {
                if (srcL.copyValue(-1, destL)) {
                    // copy global value
                    String firstUpValue = srcL.getUpValue(-1, 1);
                    if (Objects.equals("_ENV", firstUpValue)) {
                        // copy _ENV for lua52+
                        destL.getUpValue(-1, 1);
                        srcL.copyTableIfNotExists(-1, destL);
                        srcL.pop(1);
                        destL.pop(1);
                    } else {
                        if (firstUpValue != null) {
                            srcL.pop(1);
                        }
                        // copy _G for luajit
                        srcL.getGlobal("_G");
                        destL.getGlobal("_G");
                        srcL.copyTableIfNotExists(-1, destL);

                        // copy real _G
                        srcL.pushString("_LUAJAVA_G_REF");
                        srcL.getTable(-2);
                        destL.pushString("_LUAJAVA_G_REF");
                        destL.getTable(-2);
                        if (!srcL.isNil(-1) && !destL.isNil(-1)) {
                            int srcRef = srcL.toInteger(-1);
                            int destRef = destL.toInteger(-1);
                            srcL.rawGetI(LuaState.LUA_REGISTRYINDEX, srcRef);
                            destL.rawGetI(LuaState.LUA_REGISTRYINDEX, destRef);
                            srcL.copyTableIfNotExists(-1, destL);
                            srcL.pop(1);
                            destL.pop(1);
                        }

                        srcL.pop(2);
                        destL.pop(2);
                    }

                    // copy params
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
                    return true;
                }
            } finally {
                srcL.pop(1);
            }
        } finally {
            srcF.unlock();
        }
        return false;
    }

    @Override
    public Result<Object[], LuaException> submit(ILuaCallable luaCallable, int _nres, Object... params) {
        try {
            return executor.submit(() -> doSubmit(luaCallable, _nres, params)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(ILuaCallable luaCallable, Object... params) {
        executor.execute(() -> doSubmit(luaCallable, 0, params));
    }

    protected Result<Object[], LuaException> doSubmit(ILuaCallable luaCallable, int _nres, Object... params) {
        LuaPoolEntity poolEntity = getLuaState();
        LuaStateFacade srcF = luaCallable.getLuaState();
        LuaStateFacade destF = poolEntity.getLuaStateFacade();
        LuaState srcL = srcF.getLuaState();
        LuaState destL = destF.getLuaState();


        // call closure
        destF.lock();
        int top;
        try {
            top = destL.getTop();
            if (transferClosure(srcF, destF, luaCallable, params)) {
                return destF.doPcall(params.length, _nres, 0)
                        .mapResultValue(v -> {
                            if (_nres == 0) return Result.success();
                            int nres = _nres;
                            int currentTop = destL.getTop();
                            if (nres == LuaState.LUA_MULTRET) {
                                nres = currentTop - top;
                            }
                            if (currentTop - top < nres) {
                                return Result.failure(new LuaException("Invalid Number of Results .")).justCast();
                            }

                            // copy returns
                            Object[] res = new Object[nres];
                            srcF.lock();
                            try {
                                for (int i = nres - 1; i >= 0; i--) {
                                    Result<Object, ? extends LuaException> ret = destF.rawToJavaObject(-1);
                                    if (ret.isError()) return ret.justCast();
                                    res[i] = ret.getValue();
                                    if (res[i] instanceof ILuaObject) {
                                        try (LuaObject obj = (LuaObject) res[i]) {
                                            if (destL.copyValue(-1, srcL)) {
                                                res[i] = srcF.rawToJavaObject(-1).getOrSneakyThrow();
                                                srcL.pop(1);
                                            } else {
                                                res[i] = null;
                                            }
                                        }
                                    }
                                    destL.pop(1);
                                }
                            } finally {
                                srcF.unlock();
                            }
                            return Result.success(res);
                        });
            }
        } finally {
            destL.setTop(0);
            destF.unlock();
            returnLuaState(poolEntity);
        }
        return Result.success();
    }

    protected LuaStateFacade newLuaState() {
        return env.createLuaState();
    }

    protected LuaPoolEntity getLuaState() {
        poolLock.lock();
        try {
            LuaPoolEntity poolEntity = null;
            while (queue.isEmpty()) {
                if (currentPoolSize < config.getMaxSize()) {
                    currentPoolSize += 1;
                    LuaStateFacade facade = newLuaState();
                    poolEntity = new LuaPoolEntity(facade);
                    break;
                } else {
                    try {
                        freeCondition.await();
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
            if (poolEntity == null) {
                poolEntity = queue.poll();
            }
            running.add(poolEntity);
            return poolEntity;
        } finally {
            poolLock.unlock();
        }
    }

    protected void returnLuaState(LuaPoolEntity poolEntity) {
        poolLock.lock();
        try {
            if (running.remove(poolEntity) && queue.add(poolEntity)) {
                freeCondition.signal();
            }
        } finally {
            poolEntity.returned();
            poolLock.unlock();
        }
    }

    @Override
    public void close() {
        scheduled.shutdown();
        poolLock.lock();
        try {
            try {
                executor.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
            running.forEach(it -> {
                try {
                    new Thread(() -> {
                        LuaInMinecraftBukkit.instance().getLogger().warning("Waiting for LuaPool state to close: " + it.getStateId());
                        LuaInMinecraftBukkit.instance().getLogger().warning("This process will running on another thread until LuaPool state closed! Please make sure your task is not infinity task!");
                        it.close();
                        DebugLogger.debug("Closed lua state %d, this lua state be called %d times, %d milliseconds, latest run at %d",
                                it.getStateId(), it.getRunCount(), it.getTotalMilliseconds(), it.getLatestRun());
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            running.clear();

            while (!queue.isEmpty()) {
                LuaPoolEntity poll = queue.poll();
                try {
                    poll.getLuaStateFacade().close();
                    DebugLogger.debug("Closed lua state %d, this lua state be called %d times, %d milliseconds, latest run at %d",
                            poll.getStateId(), poll.getRunCount(), poll.getTotalMilliseconds(), poll.getLatestRun());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            poolLock.unlock();
        }
    }
}
