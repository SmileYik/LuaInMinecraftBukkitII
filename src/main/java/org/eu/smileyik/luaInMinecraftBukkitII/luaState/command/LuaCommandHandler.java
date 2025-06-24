package org.eu.smileyik.luaInMinecraftBukkitII.luaState.command;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.bukkit.command.CommandSender;
import org.eu.smileyik.luajava.type.ILuaCallable;

import java.lang.reflect.Method;
import java.util.Map;

public class LuaCommandHandler {
    private final Map<String, ILuaCallable> luaCallableMap;

    public LuaCommandHandler(Map<String, ILuaCallable> luaCallableMap) {
        this.luaCallableMap = luaCallableMap;
    }

    @RuntimeType
    public void intercept(@Argument(0) CommandSender sender,
                          @Argument(1) String[] args,
                          @Origin Method method) throws Exception {
        String methodName = method.getName();
        ILuaCallable iLuaCallable = luaCallableMap.get(methodName);
        if (iLuaCallable != null) {
            iLuaCallable.call(0, new Object[]{ sender, args }).justThrow();
        }
    }

    public void clear() {
        if (luaCallableMap != null) {
            luaCallableMap.clear();
        }
    }
}
