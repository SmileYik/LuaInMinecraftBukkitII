package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command;

import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandClassBuilder;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.jetbrains.annotations.NotNull;

public interface ILuaCommandClassBuilder {
    /**
     * 设定指令别名, 当且仅当该指令为顶级指令时有效
     *
     * @param aliases 别名
     * @return 构造器
     */
    ILuaCommandClassBuilder aliases(String... aliases);

    /**
     * 设置该类中所有指令都需要玩家才能执行.
     *
     * @return 该构造器
     */
    ILuaCommandClassBuilder needPlayer();

    /**
     * 设置该指令描述
     *
     * @param description 描述
     * @return 构造器
     */
    ILuaCommandClassBuilder description(String description);

    /**
     * 设置该类下所有指令所需要的权限.
     *
     * @param permission 权限
     * @return 构造器
     */
    ILuaCommandClassBuilder permission(String permission);

    /**
     * 使用指令构造器新建一个指令. 对于它的其他方法重载来讲, 该方法可能更加优雅.
     *
     * @param commandName 指令名称
     * @return 指令构造器
     */
    ILuaCommandBuilder command(@NotNull String commandName);

    /**
     * 注册一个指令.
     * table格式详细请看<code>CommandProperties</code>类
     *
     * @param table table, table 必须包含 <code>command</code> 与 <code>handler</code> 字段
     * @return 构造器
     * @throws Exception 如果给予的table不符合规范时抛出.
     * @see CommandProperties
     */
    ILuaCommandClassBuilder command(@NotNull LuaTable table) throws Exception;

    /**
     * 添加若干数量指令
     *
     * @param tables lua table 数组
     * @return 此构造器
     * @throws Exception 如果给予的table不符合规范时抛出.
     * @see CommandProperties
     * @see LuaCommandClassBuilder#command(LuaTable)
     */
    ILuaCommandClassBuilder commands(@NotNull LuaTable... tables) throws Exception;

    /**
     * 添加一个指令.
     *
     * @param callable lua闭包
     * @param command  指令名称
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command);

    /**
     * 添加一个指令.
     *
     * @param callable    lua闭包
     * @param command     指令名称
     * @param description 指令描述
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable,
                                    @NotNull String command,
                                    @NotNull String description);

    /**
     * 添加一个指令.
     *
     * @param callable lua闭包
     * @param command  指令名称
     * @param args     指令参数
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args);

    /**
     * 添加一个指令.
     *
     * @param callable    lua闭包
     * @param command     指令名称
     * @param args        指令参数
     * @param description 指令描述
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                    String description);

    /**
     * 添加一个指令.
     *
     * @param callable    lua闭包
     * @param command     指令名称
     * @param args        指令参数
     * @param description 指令描述
     * @param permission  指令权限
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                    String description, String permission);

    /**
     * 添加一个指令.
     *
     * @param callable    lua闭包
     * @param command     指令名称
     * @param args        指令参数
     * @param description 指令描述
     * @param permission  指令权限
     * @param needPlayer  是否需要玩家执行
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                    String description, String permission, boolean needPlayer);

    /**
     * 添加一个指令.
     *
     * @param callable      lua闭包
     * @param command       指令名称
     * @param args          指令参数
     * @param description   指令描述
     * @param permission    指令权限
     * @param needPlayer    是否需要玩家执行
     * @param unlimitedArgs 是否无限参数长度
     * @return 此构造器
     */
    ILuaCommandClassBuilder command(@NotNull ILuaCallable callable,
                                    @NotNull String command,
                                    String[] args, String description, String permission,
                                    boolean needPlayer, boolean unlimitedArgs);

    /**
     * 构建指令类型.
     *
     * @param metaTable     lua table,
     *                      有效字段为 command(必填),
     *                      aliases, description, permission, needPlayer, parentCommand
     * @param commandTables 与commands方法类似.
     * @return 指令类型
     * @throws Exception 如果不符合要求时抛出, 例如缺少必填字段
     * @see CommandProperties
     * @see LuaCommandClassBuilder#command(LuaTable)
     */
    Class<?> build(LuaTable metaTable, LuaTable... commandTables) throws Exception;

    Class<?> build(@NotNull String command);

    Class<?> build(@NotNull String command, String parentCommand);
}
