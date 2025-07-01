package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event;

import org.bukkit.event.EventPriority;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public interface ILuaEventListenerBuilder {
    /**
     * 订阅一个事件
     *
     * @param eventClassName 事件全类名, 常见类名可以忽略包路径
     * @param closure        事件闭包, 固定一个形参, 为监听的事件实例.
     * @return 此构建器
     * @throws ClassNotFoundException 如果该事件类型不存在则抛出.
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * 订阅一个事件
     *
     * @param eventClassName 事件全类名, 常见类名可以忽略包路径
     * @param eventPriority  事件优先级
     * @param closure        事件闭包, 固定一个形参, 为监听的事件实例.
     * @return 此构建器
     * @throws ClassNotFoundException 如果该事件类型不存在则抛出.
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull EventPriority eventPriority,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * 订阅一个事件
     *
     * @param eventClassName 事件全类名, 常见类名可以忽略包路径
     * @param eventPriority  事件优先级, 包含<code>LOWEST</code> <code>LOW</code> <code>NORMAL</code>
     *                       <code>HIGH</code> <code>HIGHEST</code> <code>MONITOR</code>
     * @param closure        事件闭包, 固定一个形参, 为监听的事件实例.
     * @return 此构建器
     * @throws ClassNotFoundException 如果该事件类型不存在则抛出.
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull String eventPriority,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * 订阅一个事件
     *
     * @param eventClassName  事件全类名, 常见类名可以忽略包路径
     * @param eventPriority   事件优先级
     * @param ignoreCancelled 是否忽略已取消的事件.
     * @param closure         事件闭包, 固定一个形参, 为监听的事件实例.
     * @return 此构建器
     * @throws ClassNotFoundException 如果该事件类型不存在则抛出.
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull EventPriority eventPriority,
                                       boolean ignoreCancelled,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * 订阅一个事件
     *
     * @param eventClassName  事件全类名, 常见类名可以忽略包路径
     * @param eventPriority   事件优先级, 包含<code>LOWEST</code> <code>LOW</code> <code>NORMAL</code>
     *                        <code>HIGH</code> <code>HIGHEST</code> <code>MONITOR</code>
     * @param ignoreCancelled 是否忽略已取消的事件.
     * @param closure         事件闭包, 固定一个形参, 为监听的事件实例.
     * @return 此构建器
     * @throws ClassNotFoundException 如果该事件类型不存在则抛出.
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull String eventPriority,
                                       boolean ignoreCancelled,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * 订阅一个事件
     *
     * @param eventClassName  事件全类名, 常见类名可以忽略包路径
     * @param ignoreCancelled 是否忽略已取消的事件.
     * @param closure         事件闭包, 固定一个形参, 为监听的事件实例.
     * @return 此构建器
     * @throws ClassNotFoundException 如果该事件类型不存在则抛出.
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       boolean ignoreCancelled,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * 订阅一个事件, 传入LuaTable类型, 并且必须包含<code>event</code>和<code>handler</code>字段.
     * <code>event</code>字段为文本类型, 是要订阅的事件的全类名.
     * <code>handler</code>字段为Lua闭包, 并且包含一个形参.
     *
     * @param table LuaTable
     * @return 此构建器
     * @throws Exception 如果LuaTable不符合要求则抛出
     */
    ILuaEventListenerBuilder subscribe(@NotNull LuaTable table) throws Exception;

    /**
     * 与<code>subscribe(LuaTable)</code>类似, 但是是接受一个LuaTable数组(数组风格LuaTable),
     * 以批量订阅事件.
     *
     * @param tables table组成的数组, 形似与<code>local tables = {{}, {}, {}}</code>
     * @return 此构建器
     * @throws Exception 如果LuaTable不符合要求则抛出
     */
    ILuaEventListenerBuilder subscribes(@NotNull LuaTable... tables) throws Exception;

    /**
     * 构造未监听的事件实例.
     *
     * @return 未监听的事件实例
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    LuaUnregisteredListener build()
            throws NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException;
}
