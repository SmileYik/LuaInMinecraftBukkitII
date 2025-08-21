package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState;

import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.ILuaCommandClassBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event.ILuaEventListenerBuilder;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;

import java.io.File;

public interface ILuaEnv {
    /**
     * 注册事件监听器. 如果需要注册监听器请使用 <code>listenerBuilder()</code>.
     * @param name     监听器名字
     * @param listener 监听器
     */
    void registerEventListener(String name, Listener listener);

    /**
     * 取消注册事件监听器.
     * @param name 注册监听器时提供的监听器名, 名字在同一个lua环境中必须不同(唯一).
     */
    void unregisterEventListener(String name);

    /**
     * 获取事件监听构造器, 通过此构造器可以构造Bukkit Listener接口实例.
     * <h1>Lua中的使用示例</h1>
     * <h2>构建一个 <code>PlayerJoinEvent</code></h2>
     * 以下是构建并注册一个单个Bukkit事件监听的例子.
     * 该示例将会监听玩家加入服务器事件, 并且在玩家加入服务器时发送玩家 "Hello 玩家名" 消息.
     * 并且将此事件注册监听时, 命名为 "MyPlayerJoinEvent" 以方便后续取消监听.
     * <pre><code>
     *     luaBukkit.env:listenerBuilder()
     *         :subscribe("PlayerJoinEvent",
     *             function (event)
     *                 event:getPlayer():sendMessage("Hello " .. event:getPlayer():getName())
     *             end
     *         )
     *         :build()
     *         :register("MyPlayerJoinEvent")
     * </code></pre>
     * <h2>构建多个事件</h2>
     * 在本例子中将会监听玩家进入服务器事件以及离开服务器事件.
     * <pre><code>
     *     luaBukkit.env:listenerBuilder()
     *         :subscribe("PlayerJoinEvent",
     *             function (event)
     *                 event:getPlayer():sendMessage("Hello " .. event:getPlayer():getName())
     *             end
     *         )
     *         :subscribe("org.bukkit.event.player.PlayerQuitEvent",
     *             function (event)
     *                 luaBukkit.log:info("Someone leaving: " .. event:getQuitMessage())
     *                 luaBukkit.env:unregisterEventListener("MyEvents")
     *             end
     *         )
     *         :build()
     *         :register("MyEvents")
     * </code></pre>
     * @return 事件监听构造器.
     */
    ILuaEventListenerBuilder listenerBuilder();

    /**
     * 获取指令类构造器.
     * @return 指令类构造器.
     */
    ILuaCommandClassBuilder commandClassBuilder();

    /**
     * 注册指令
     * @param rootCommand 指令名称.
     * @param classes     指令类型.
     * @return 注册结果.
     */
    Result<Boolean, Exception> registerCommand(String rootCommand, Class<?>... classes);

    /**
     * 注册指令
     * @param rootCommand 指令名称.
     * @param aliases     指令别名
     * @param classes     指令类型.
     * @return 注册结果.
     */
    Result<Boolean, Exception> registerCommand(String rootCommand, String[] aliases, Class<?>... classes);

    /**
     * 注册清理器
     * @param cleaner 清理器, 是一个 lua function closure.
     */
    void registerCleaner(ILuaCallable cleaner);

    /**
     * 注册软重载闭包. 这个闭包将会在软重启Lua环境时调用.
     * @param luaCallable 闭包
     * @return 如果注册失败则返回失败信息.
     */
    Result<Void, String> registerSoftReload(ILuaCallable luaCallable);

    /**
     * 将传入的闭包转为 Lua 池用闭包, 该 Lua 池用闭包会运行在其他 Lua 状态机中,
     * 在其他 Lua 状态机运行时, 会自动传输该闭包下所用到的一切 `local` 标记的局部变量.
     * 在使用中尽量让闭包与当前环境的全局变量无关, 并且尽量使用 Java 实例来传递数值.
     * 若一定需要使用某个全局变量, 请以形参方式传输.
     * @param callable 闭包
     * @return 池化闭包
     */
    public ILuaCallable pooledCallable(ILuaCallable callable);

    /**
     * 获取文件路径.
     * @param path 文件名
     * @return 文件实际存放的路径.
     */
    String path(String path);

    /**
     * 获取文件路径.
     * @param paths 文件名数组
     * @return 文件实际存放的路径.
     */
    String path(String... paths);

    /**
     * 获取文件
     * @param path 文件名
     * @return 对应文件实例
     */
    File file(String path);

    /**
     * 获取文件
     * @param paths 文件名
     * @return 对应文件实例
     */
    File file(String... paths);
}
