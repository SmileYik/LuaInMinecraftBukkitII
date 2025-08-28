package org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool;

import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.LuaObject;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;

/**
 * 池化闭包. 该闭包在实际运行时, 会被提交到 Lua 池中的状态机进行运行.
 */
public class PooledLuaCallable implements ILuaCallable {

    private final ILuaCallable callable;
    private final LuaPool pool;

    public PooledLuaCallable(ILuaCallable callable, LuaPool pool) {
        this.callable = callable;
        this.pool = pool;
    }

    public static ILuaCallable of(ILuaCallable luaCallable, LuaPool pool) {
        return new PooledLuaCallable(luaCallable, pool);
    }

    @Override
    public Result<Object, ? extends LuaException> call(Object... args) {
        return pool.submit(callable, 1, args)
                .mapResultValue(objs -> Result.success(objs.length > 0 ? objs[0] : null));
    }

    @Override
    public Result<Object[], ? extends LuaException> call(int nres, Object... args) {
        return pool.submit(callable, nres, args);
    }

    @Override
    public LuaStateFacade getLuaState() {
        return callable.getLuaState();
    }

    @Override
    public boolean isClosed() {
        return callable.isClosed();
    }

    @Override
    public void push() {
        callable.push();
    }

    @Override
    public int type() {
        return callable.type();
    }

    @Override
    public long getLuaPointer() {
        return callable.getLuaPointer();
    }

    @Override
    public boolean isRawEqualInLua(LuaObject obj) {
        return callable.isRawEqualInLua(obj);
    }
}
