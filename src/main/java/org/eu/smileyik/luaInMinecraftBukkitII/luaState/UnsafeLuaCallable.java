package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.LuaObject;
import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.IInnerLuaObject;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaFunction;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.util.Arrays;

import static org.eu.smileyik.luajava.LuaState.*;

/**
 * 此类的 `call` 方法经过重写, 使其调用时并不会请求锁.
 * <strong>这可能会导致段错误, 从而引发JVM崩溃!</strong>
 * 该类仅作为实验目的, 应该尽可能避免不使用该类.
 */
public class UnsafeLuaCallable extends LuaFunction {
    /**
     * <strong>SHOULD NOT USE CONSTRUCTOR DIRECTLY, EXPECT YOU KNOW WHAT YOU ARE DOING</strong>
     *
     * @param L     lua state
     * @param index index
     * @see LuaObject#create(LuaStateFacade, int)
     */
    protected UnsafeLuaCallable(LuaStateFacade L, int index) {
        super(L, index);
    }

    public static ILuaCallable of(ILuaCallable luaCallable) {
        LuaStateFacade facade = luaCallable.getLuaState();
        facade.lock();
        try (LuaObject innerObject = (LuaObject) luaCallable) {
            innerObject.rawPush();
            return new UnsafeLuaCallable(facade, -1);
        } finally {
            facade.unlock();
        }
    }

    /**
     * Calls the object represented by <code>this</code> using Lua function pcall. Returns 1 object
     *
     * @param args -
     *             Call arguments
     * @return Object - Returned Object
     */
    public Result<Object, ? extends LuaException> call(Object... args) {
        DebugLogger.debug(ele -> {
            DebugLogger.debug(ele, "[Unsafe] LuaState call: %s", Arrays.toString(args));
        });

        if (isClosed()) return Result.failure(new LuaException("This lua state is closed!"));
        return pcall(args, 1).mapValue(it -> it[0]);
    }

    /**
     * Calls the object represented by <code>this</code> using Lua function pcall.
     *
     * @param nres -
     *             Number of objects returned
     * @param args -
     *             Call arguments
     * @return Object[] - Returned Objects
     */
    public Result<Object[], ? extends LuaException> call(int nres, Object... args) {
        DebugLogger.debug(ele -> {
            DebugLogger.debug(ele, "[Unsafe] LuaState call #%d: %s", nres, Arrays.toString(args));
        });

        if (isClosed()) return Result.failure(new LuaException("This lua state is closed!"));
        return pcall(args, nres);
    }

    private Result<Object[], ? extends LuaException> pcall(Object[] args, int _nres) {
        LuaState state = luaState.getLuaState();
        IInnerLuaObject innerObject = (IInnerLuaObject) this;
        final int top = state.getTop();
        int nargs = 0;

        try {
            // push function and push params.
            innerObject.rawPush();
            if (args != null) {
                nargs = args.length;
                for (Object obj : args) {
                    Result<Void, ? extends LuaException> pushResult = luaState.rawPushObjectValue(obj);
                    if (pushResult.isError()) {
                        return pushResult.justCast();
                    }
                }
            }

            return pcall(nargs, _nres, 0).mapResultValue((v) -> {
                int nres = _nres;
                int currentTop = luaState.getTop();
                if (nres == LuaState.LUA_MULTRET) {
                    nres = currentTop - top;
                }
                if (currentTop - top < nres) {
                    return Result.failure(new LuaException("Invalid Number of Results .")).justCast();
                }

                Object[] res = new Object[nres];
                for (int i = nres - 1; i >= 0; i--) {
                    Result<Object, ? extends LuaException> ret = luaState.rawToJavaObject(-1);
                    if (ret.isError()) return ret.justCast();
                    res[i] = ret.getValue();
                    luaState.pop(1);
                }
                return Result.success(res);
            });
        } finally {
            luaState.setTop(top);
        }
    }

    private Result<Void, LuaException> pcall(int nArgs, int nResults, int errFunc) {
        LuaState luaState = getLuaState().getLuaState();
        int exp = luaState.pcall(nArgs, nResults, errFunc);
        if (exp != 0) {
            String err = getErrorMessage(exp);
            return Result.failure(new LuaException(err), err);
        }
        return Result.success();
    }

    protected String getErrorMessage(int err) {
        LuaState luaState = getLuaState().getLuaState();
        String ret = null;
        switch (err) {
            case LUA_ERRRUN:
                ret = "Runtime error. ";
                break;
            case LUA_ERRMEM:
                ret = "Memory allocation error. ";
                break;
            case LUA_ERRERR:
                ret = "Error while running the error handler function.";
                break;
            default:
                break;
        }
        if (luaState.isString(-1)) {
            ret = ret == null ? luaState.toString(-1) : (ret + luaState.toString(-1));
        }
        return ret;
    }
}
