package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState;

import org.bukkit.scheduler.BukkitTask;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.reflect.ReflectUtil;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaArray;

import java.lang.reflect.Array;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface LuaHelper {
    /**
     * 构建一个 Runnable 实例.
     * @param callable Lua 闭包
     * @return Runnable 实例.
     */
    public static Runnable runnable(ILuaCallable callable) {
        return callable::call;
    }

    /**
     * 构建一个 Consumer 实例
     * @param callable Lua 闭包
     * @return Consumer 实例
     * @param <T> 泛型
     */
    public static <T> Consumer<T> consumer(ILuaCallable callable) {
        return callable::call;
    }

    /**
     * 构建一个 Function 实例
     * @param callable Lua 闭包
     * @return Function 实例
     * @param <T> 输入
     * @param <R> 输出
     */
    public static <T, R> Function<T, R> function(ILuaCallable callable) {
        return t -> (R) callable.call(t).getOrSneakyThrow();
    }

    /**
     * 同步运行 Lua 闭包
     * @param callable Lua 闭包
     * @return Future<Object>
     */
    public static CompletableFuture<Object> syncCall(ILuaCallable callable) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Object result = null;
            try {
                result = callable.call().getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        });
        return future;
    }

    /**
     * 同步运行 Lua 闭包, 并传入参数.
     * @param callable Lua 闭包
     * @param params   传入 Lua 闭包的参数.
     * @return Future<Object>
     */
    public static CompletableFuture<Object> syncCall(ILuaCallable callable, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Object result = null;
            try {
                result = callable.call(params).getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        });
        return future;
    }

    /**
     * 同步延迟运行 Lua 闭包
     * @param callable Lua 闭包
     * @param tick     延迟, 单位: tick
     * @return Future<Object>
     */
    public static CompletableFuture<Object> syncCallLater(ILuaCallable callable, long tick) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Object result = null;
            try {
                result = callable.call().getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        }, tick);
        return future;
    }

    /**
     * 同步延迟运行 Lua 闭包
     * @param callable Lua 闭包
     * @param tick     延迟, 单位: tick
     * @param params   传入闭包的参数.
     * @return Future<Object>
     */
    public static CompletableFuture<Object> syncCallLater(ILuaCallable callable, long tick, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {Object result = null;
            try {
                result = callable.call(params).getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        }, tick);
        return future;
    }

    /**
     * 同步执行计时器. 调用该方法则有责任管理该计时器, 请在不需要的时候释放它.
     * @param callable Lua 闭包
     * @param delay    延迟执行, 单位: tick
     * @param period   间隔执行, 单位: tick
     * @return BukkitTask.
     */
    public static BukkitTask syncTimer(ILuaCallable callable, long delay, long period) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, () -> callable.call(), delay, period);
    }

    /**
     * 同步执行计时器. 调用该方法则有责任管理该计时器, 请在不需要的时候释放它.
     * @param callable Lua 闭包
     * @param delay    延迟执行, 单位: tick
     * @param period   间隔执行, 单位: tick
     * @param params   传入闭包参数.
     * @return BukkitTask.
     */
    public static BukkitTask syncTimer(ILuaCallable callable, long delay, long period, Object ... params) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, () -> callable.call(params), delay, period);
    }

    /**
     * 异步调用 Lua 闭包.
     * @param callable Lua 闭包
     * @return Future<Object>
     */
    public static CompletableFuture<Object> asyncCall(ILuaCallable callable) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Object result = null;
            try {
                result = callable.call().getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        });
        return future;
    }

    /**
     * 异步调用 Lua 闭包.
     * @param callable Lua 闭包
     * @param params   传入闭包的参数
     * @return Future<Object>
     */
    public static CompletableFuture<Object> asyncCall(ILuaCallable callable, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Object result = null;
            try {
                result = callable.call(params).getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        });
        return future;
    }

    /**
     * 异步调用 Lua 闭包.
     * @param callable Lua 闭包
     * @param tick     延迟执行, 单位: tick
     * @return Future<Object>
     */
    public static CompletableFuture<Object> asyncCallLater(ILuaCallable callable, long tick) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Object result = null;
            try {
                result = callable.call().getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        }, tick);
        return future;
    }

    /**
     * 异步调用 Lua 闭包.
     * @param callable Lua 闭包
     * @param tick     延迟执行, 单位: tick
     * @param params   传入闭包的参数
     * @return Future<Object>
     */
    public static CompletableFuture<Object> asyncCallLater(ILuaCallable callable, long tick, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Object result = null;
            try {
                result = callable.call(params).getOrSneakyThrow();
            } finally {
                future.complete(result);
            }
        }, tick);
        return future;
    }

    /**
     * 异步调用 Lua 闭包.
     * @param callable Lua 闭包
     * @param delay    延迟执行, 单位: tick
     * @param period   间隔执行, 单位: tick
     * @return Future<Object>
     */
    public static BukkitTask asyncTimer(ILuaCallable callable, long delay, long period) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> callable.call(), delay, period);
    }

    /**
     * 异步调用 Lua 闭包.
     * @param callable Lua 闭包
     * @param delay    延迟执行, 单位: tick
     * @param period   间隔执行, 单位: tick
     * @param params   传入闭包的参数
     * @return Future<Object>
     */
    public static BukkitTask asyncTimer(ILuaCallable callable, long delay, long period, Object ... params) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> callable.call(params), delay, period);
    }

    /**
     * 将 lua 中的数组风格表转换为指定类型的 Java 数组.
     * @param className 类型全类名
     * @param array     lua 中数组风格表
     * @return Java 数组
     * @throws ClassNotFoundException 如果类型不存在则抛出
     */
    public static Optional<Object> castArray(String className, LuaArray array) throws ClassNotFoundException {
        return castArray(Class.forName(className), array);
    }

    /**
     * 将 lua 中的数组风格表转换为指定类型的 Java 数组.
     * @param type  类型
     * @param array lua 中数组风格表
     * @return Java 数组
     */
    public static Optional<Object> castArray(Class<?> type, LuaArray array) {
        ParamRef<Object> ref = ParamRef.wrapper();
        byte result = ConvertablePriority.isConvertableType(Integer.MAX_VALUE, array, type, ref);
        return result == ConvertablePriority.NOT_MATCH ?
                Optional.empty() : Optional.of(ref.getParam());
    }
}
