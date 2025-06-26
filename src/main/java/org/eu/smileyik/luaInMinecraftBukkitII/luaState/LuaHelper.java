package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.bukkit.scheduler.BukkitTask;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luajava.reflect.ConvertablePriority;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaArray;
import org.eu.smileyik.luajava.util.ParamRef;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface LuaHelper {
    public static Runnable runnable(ILuaCallable callable) {
        return callable::call;
    }

    public static <T> Consumer<T> consumer(ILuaCallable callable) {
        return callable::call;
    }

    public static <T, R> Function<T, R> function(ILuaCallable callable) {
        return t -> (R) callable.call(t).getOrSneakyThrow();
    }

    public static CompletableFuture<Object> syncCall(ILuaCallable callable) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                Object result = callable.call().getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    public static CompletableFuture<Object> syncCall(ILuaCallable callable, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                Object result = callable.call(params).getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    public static CompletableFuture<Object> syncCallLater(ILuaCallable callable, long tick) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                Object result = callable.call().getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        }, tick);
        return future;
    }

    public static CompletableFuture<Object> syncCallLater(ILuaCallable callable, long tick, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                Object result = callable.call(params).getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        }, tick);
        return future;
    }

    public static BukkitTask syncTimer(ILuaCallable callable, long delay, long period) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, () -> callable.call(), delay, period);
    }

    public static BukkitTask syncTimer(ILuaCallable callable, long delay, long period, Object ... params) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, () -> callable.call(params), delay, period);
    }

    public static CompletableFuture<Object> asyncCall(ILuaCallable callable) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Object result = callable.call().getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    public static CompletableFuture<Object> asyncCall(ILuaCallable callable, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Object result = callable.call(params).getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    public static CompletableFuture<Object> asyncCallLater(ILuaCallable callable, long tick) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                Object result = callable.call().getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        }, tick);
        return future;
    }

    public static CompletableFuture<Object> asyncCallLater(ILuaCallable callable, long tick, Object ... params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                Object result = callable.call(params).getOrSneakyThrow();
                future.complete(result);
            } finally {
                future.complete(null);
            }
        }, tick);
        return future;
    }

    public static BukkitTask asyncTimer(ILuaCallable callable, long delay, long period) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> callable.call(), delay, period);
    }

    public static BukkitTask asyncTimer(ILuaCallable callable, long delay, long period, Object ... params) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        return plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> callable.call(params), delay, period);
    }

    public static Optional<Object> castArray(String className, LuaArray array) throws ClassNotFoundException {
        return castArray(Class.forName(className), array);
    }

    public static Optional<Object> castArray(Class<?> type, LuaArray array) {
        ParamRef<Object> ref = ParamRef.wrapper();
        byte result = ConvertablePriority.isConvertableType(Integer.MAX_VALUE, array, type, ref);
        return result == ConvertablePriority.NOT_MATCH ?
                Optional.empty() : Optional.of(ref.getParam());
    }
}
