package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command;

import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandClassBuilder;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.jetbrains.annotations.NotNull;

/**
 * 指令类建造器. 指令类是一个包含多条指令的集合, 并且指令类也有自己的名称.
 * <p>
 *     举个例子, 现有如下指令:
 *     <pre><code>
 *         /item get [name]
 *         /item store [name]
 *         /item nbt read [key]
 *         /item nbt write [key] [value]
 *     </code></pre>
 * </p>
 * <p>
 *     既然指令类是一个包含多条指令的集合, 而每一条实际指令方法之间又没有层级关系,
 *     只能匹配指令的固定词还有它的参数, 那么, 指令之间的层级关系就由指令类之间的层级关系生成.
 *     以上的四条指令可以抽取成两个指令类, 分别如下:
 *     <li>
 *         item 指令类: 包含指令 <code>get [name]</code>, <code>store [name]</code>.
 *     </li>
 *     <li>
 *         nbt 指令类: 包含指令 <code>read [key]</code>, <code>write [key] [value]</code>
 *     </li>
 * </p>
 * <p>
 *     为了让<code>item</code>指令类与<code>nbt</code>指令类之间有层级关系,我们可以在构建时,
 *     先构造<code>item</code>指令类, 直接使用<code>build(String)</code>方法,
 *     再构造<code>nbt</code>指令类, 使用<code>build(String, String)</code>方法.
 *     之后注册指令时, 就可以将两个指令类型一起注册. 而在 Lua 中的实际注册代码可以参考以下代码:
 *     <pre><code>
 *          -- 构造 item 指令类
 *          local itemCommandClass = luaBukkit.env:commandClassBuilder()
 *             :command("get")      -- get 指令
 *                 :args({"name"})
 *                 :description("get a item")
 *                 :handler(function(sender, args) doSomething() end)
 *             :command("store")    -- store 指令
 *                 :args({"name"})
 *                 :description("store a item and named it")
 *                 :handler(function(sender, args) doSomething() end)
 *             :build("item")
 *
 *          -- 构造 nbt 指令类
 *          local nbtCommandClass = luaBukkit.env:commandClassBuilder()
 *             :command("read")     -- read 指令
 *                 :args({"key"})
 *                 :description("read item's nbt key")
 *                 :handler(function(sender, args) doSomething() end)
 *             :command("write")    -- write 指令
 *                 :args({"key", "value"})
 *                 :description("write key-value to item' nbt")
 *                 :handler(function(sender, args) doSomething() end)
 *             :build("nbt", "item") -- 设定 nbt 类的父指令类为 item 类
 *
 *          -- 注册指令, 并且将最顶层, 也就是没有父指令类的指令类名称写到第一个形参中,
 *          -- 并且将两个指令类组成数组风格的Table, 传入第二个形参.
 *          local result = luaBukkit.env:registerCommand("item", {itemCommandClass, nbtCommandClass})
 *          if result:isError() then luaBukkit.log:info("Register command failed!") end
 *     </code></pre>
 * </p>
 *
 */
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

    /**
     * 构建指令
     * @param command 根指令名
     * @return 构建好的指令类
     */
    Class<?> build(@NotNull String command);

    /**
     * 构建指令, 并将次类型归类在指定父级指令下. 例如有两个指令类,
     * <li>
     *     指令类1的顶级指令名为 <code>item</code>, 其中包含子指令: <code>get</code>, <code>set</code>
     * </li>
     * <li>
     *     指令类2的顶级指令名为 <code>nbt</code>, 其中包含子指令: <code>read</code>, <code>clear</code>.
     * </li>
     * 此时构建指令时, 将指令2的父级指令名设置为指令1, 注册完指令后, 实际生成的指令是这样的:
     * <pre><code>
     *      /item get
     *      /item set
     *      /item nbt read
     *      /item nbt clear
     * </code></pre>
     * @param command       顶级指令名
     * @param parentCommand 父级指令名
     * @return 构建好的指令类
     */
    Class<?> build(@NotNull String command, String parentCommand);
}
