package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event;

import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.luajava.type.ILuaCallable;

/**
 * 事件监听JavaBean类型.
 */
@Data
@ToString
public class LuaEventListenerProperty {
    /**
     * 事件类型全类名, 必填.
     */
    private String event;
    /**
     * 事件优先级, 选填, 默认为<code>NORMAL</code>, 可用值为
     * <code>LOWEST</code>, <code>LOW</code>, <code>NORMAL</code>,
     * <code>HIGH</code>, <code>HIGHEST</code>, <code>MONITOR</code>.
     * 并且大小写不敏感.
     */
    private String priority;
    /**
     * 是否忽略已经取消的事件. 选填, 默认为false
     */
    private boolean ignoreCancelled = false;
    /**
     * 事件处理器, 必填. 该字段为一个Lua闭包, 并且闭包应该拥有一个形参,
     * 用于接收事件实例.
     */
    private ILuaCallable handler;
}
