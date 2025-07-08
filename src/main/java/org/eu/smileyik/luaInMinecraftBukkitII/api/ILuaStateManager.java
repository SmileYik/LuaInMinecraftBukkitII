package org.eu.smileyik.luaInMinecraftBukkitII.api;

import org.bukkit.plugin.Plugin;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStatePluginEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILuaStateManager extends AutoCloseable {
    /**
     * 创建插件用Lua环境. <br/>
     * 插件应该自行管理该环境.
     * @param plugin 插件实例
     * @return Lua 环境.
     */
    ILuaStatePluginEnv createPluginEnv(@NotNull Plugin plugin);

    /**
     * 创建插件用Lua环境. <br/>
     * 插件应该自行管理该环境.
     * @param plugin            插件实例
     * @param ignoreAccessLimit 忽略访问限制.
     * @return Lua 环境.
     */
    ILuaStatePluginEnv createPluginEnv(@NotNull Plugin plugin, boolean ignoreAccessLimit);

    /**
     * 获取 Lua 实例.
     * @param plugin 插件
     * @return 如果没有则返回 null
     */
    @Nullable
    ILuaStatePluginEnv getPluginEnv(@NotNull Plugin plugin);

    /**
     * 销毁插件 Lua 环境.
     * @param plugin 插件实例.
     */
    void destroyPluginEnv(@NotNull Plugin plugin);

    /**
     * 获取 Lua 实例
     * @param id 实例 Id
     * @return Lua 实例
     */
    @Nullable
    ILuaStateEnv getEnv(String id);

    /**
     * 关闭并释放资源
     */
    void close();

    /**
     * 重新加载指定环境中的初始化脚本.
     * @param id
     */
    void reloadEnvScript(String id);

    /**
     * 重载整体 Lua 环境.
     * @param config
     */
    void reload(Config config);
}
