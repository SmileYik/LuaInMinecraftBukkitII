package org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.command;

import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.luajava.type.ILuaCallable;

/**
 * 与LuaTable关联, LuaTable中包含下述同名同类型字段, 将会直接转化为本实体中的字段.
 * <p>String[] 类型可以直接使用数组风格的LuaTable, 例如 <code>local array = {'a', 'b'}</code></p>
 * <p>
 *     ILuaCallable 类型为Lua中的function闭包, 并且这个闭包需要有两个形参,
 *     第一个形参为 <code>sender</code>, 代表谁执行的指令,
 *     第二个形参为 <code>args</code>, 代表指令参数.
 *     例如 <code>local callable = function(sender, args) end</code>
 * </p>
 *
 */
@Data
@ToString
public class CommandProperties {
    /**
     * 指令名称, 必填
     */
    private String command = null;

    /**
     * 父指令名称, 可选, 用于标记一个指令类的父指令类型
     */
    private String parentCommand = null;

    /**
     * 指令别名, 可选, 构建顶级指令类以及注册指令时可能需要用到.
     */
    private String[] aliases = null;

    /**
     * 指令参数, 可选, 仅在添加指令方法时才会用到(也就是调用command方法时需要)
     */
    private String[] args = null;

    /**
     * 指令描述, 可选
     */
    private String description = null;

    /**
     * 指令权限, 可选
     */
    private String permission = null;

    /**
     * 是否只能由玩家执行指令, 可选
     */
    private boolean needPlayer = false;

    /**
     * 是否无限长度指令参数, 可选, 与args字段类似.
     */
    private boolean unlimitedArgs = false;

    /**
     * 指令处理器, 在添加指令方法时才会用到(也就是调用command方法时需要, 此时变为必填)
     * <p>
     *     ILuaCallable 类型为Lua中的function闭包, 并且这个闭包需要有两个形参,
     *     第一个形参为 <code>sender</code>, 代表谁执行的指令,
     *     第二个形参为 <code>args</code>, 代表指令参数.
     *     例如 <code>local callable = function(sender, args) end</code>
     * </p>
     */
    private ILuaCallable handler = null;
}
