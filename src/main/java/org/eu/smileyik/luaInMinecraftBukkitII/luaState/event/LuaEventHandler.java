package org.eu.smileyik.luaInMinecraftBukkitII.luaState.event;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.eu.smileyik.luajava.type.ILuaCallable;

import java.lang.reflect.Method;
import java.util.Map;

public class LuaEventHandler {
    private final Map<String, ILuaCallable> luaCallableMap;

    public LuaEventHandler(Map<String, ILuaCallable> luaCallableMap) {
        this.luaCallableMap = luaCallableMap;
    }

    @RuntimeType
    public Object intercept(@Argument(0) Object event,
                            @Origin Method method) throws Exception {
        String methodName = method.getName();
        ILuaCallable iLuaCallable = luaCallableMap.get(methodName);
        if (iLuaCallable != null) {
            iLuaCallable.call(0, event).justThrow();
        }
        return null;
    }

    public void clear() {
        if (luaCallableMap != null) {
            luaCallableMap.clear();
        }
    }
}
