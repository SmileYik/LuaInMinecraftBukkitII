package org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.command;

import org.eu.smileyik.luajava.type.ILuaCallable;

/**
 * 指令构造器.
 */
public class LuaCommandBuilder {
    private final LuaCommandClassBuilder builder;
    private final String command;
    private String[] args = null;
    private String description = null;
    private String permission = null;
    private boolean needPlayer = false;
    private boolean unlimitedArgs = false;

    protected LuaCommandBuilder(LuaCommandClassBuilder builder, String command) {
        this.builder = builder;
        this.command = command;
    }

    /**
     * 指令所需要的参数, 这里的参数用于匹配指令长度, 并且给予执行指令人员提示.
     * 例如这个指令: <code>/myCommand kill [player_name]</code>, <code>kill</code>
     * 是指令名称, 而<code>player_name</code>是一个由执行者提供的参数.
     * 所以构建这个指令时, 需要使用这个方法去声明需要提供的参数.
     * 对于该例子来说, 可以这样实现:
     * <pre><code>
     *     luaBukkit.env:commandClassBuilder()
     *         :command("kill")
     *             :args({"player_name"})
     *             :handler(function (sender, args) doSomeThing() end)
     *         :build("myCommand")
     * </code></pre>
     * @param args 指令参数
     * @return 构造器
     */
    public LuaCommandBuilder args(String ... args) {
        this.args = args;
        return this;
    }

    /**
     * 添加对该指令的简单描述.
     * @param description 描述
     * @return 构造器
     */
    public LuaCommandBuilder desc(String description) {
        this.description = description;
        return this;
    }

    /**
     * 添加对该指令的简单描述.
     * @param description 描述
     * @return 构造器
     */
    public LuaCommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * 添加使用该指令时的权限校验.
     * @param permission 权限
     * @return 构造器
     */
    public LuaCommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * 设定这个指令只能玩家执行.
     * @return 构造器
     */
    public LuaCommandBuilder needPlayer() {
        this.needPlayer = true;
        return this;
    }

    /**
     * 设定这个指令的参数为无限长度, 匹配这个指令时会无视args中的实际长度.
     * 这在设定一些含空格的参数时特别有效.
     * @return 构造器
     */
    public LuaCommandBuilder unlimitedArgs() {
        this.unlimitedArgs = true;
        return this;
    }

    /**
     * 设置这个指令的处理器并完成该指令构造.
     * @param callable lua闭包
     * @return 指令类构造器
     */
    public LuaCommandClassBuilder handler(ILuaCallable callable) {
        builder.command(callable, command, args, description, permission, needPlayer, unlimitedArgs);
        return builder;
    }
}
