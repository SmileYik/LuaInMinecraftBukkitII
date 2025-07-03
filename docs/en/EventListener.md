> Last updated on July 03, 2025 | [历史记录](https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/en/EventListener.md)

> This page corresponds to the latest version of the LuaInMinecraftBukkit II plugin. For historical documentation, you can find the history of this page.

> **!! The content of this file has machine translation !!** | [Origin](../EventListener.md)

In this document, I will tell you how to subscribe Bukkit events in Lua.

## Preparation

Before we begin, we assume that you have some understanding of Bukkit events. 
The [Bukkit Javadoc](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/package-summary.html) can help you understand what events are in Bukkit. 
We will also need this document later.

## Parameters required to listen to Bukkit events

Before register event listener to Bukkit, four parameters are required:

+ Event Type: required
+ Event Handler: required
+ [Event Priority](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html): default NORMAL, you can pick one in `HIGH`, `HIGHEST`, `NORMAL`, `LOW`, `LOWEST`, `MONITOR`
+ [Ignore Cancelled Event]: default `false`

### Event Types

The event type is a string, which is the fully qualified name of the class in Java.

For example, [BlockBreakEvent](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockBreakEvent.html) is an event which will trigered by player break blocks,
It's class name can be found below Javadoc's title:

![org.bukkit.event.block.BlockBreakEvent](./images/EventListener.1.png)

The marked line is class name of `BlockBreakEvent`: `org.bukkit.event.block.BlockBreakEvent`

### Event Handler

Event handler is a lua function. It receives the event type instance as a parameter. 
Generally speaking, this function looks like:

```lua
-- event is the instance object of target event.
function onEvent(event)
    -- handle event
end
```

## Subscribe Event in Lua

Before subscribing to an event, you must first get the event listener builder, 
which can be obtained through the `listenerBuilder()` method in the global variable `luaBukkit.env`.

There are many ways to listen to events through the event listener builder, listed below.

```lua
luaBukkit.env:listenerBuilder()
    -- Method 1: Basic subscription event
    :subscribe("Event Class Name", 
    function(event)
        -- do something.
    end)
    -- Method 1: Basic subscription event with event priority
    :subscribe("Event Class Name", "Event Priority",
    function(event)
        -- do something.
    end)
    -- Method 3: Ignore Cancelled event
    :subscribe("Event Class Name", true/false,
    function(event)
        -- do something.
    end)
    -- Method 4: Full
    :subscribe("Event Class Name", "Event Priority", true/false,
    function(event)
        -- do something.
    end)
    -- Method 5: Use Lua Table
    :subscribe({
        event = "Event Class Name",
        priority = "Event Priority",
        ignoreCancelled = true/false,
        handler = function(event)
            -- do something.
        end
    })
    -- Method 6: Use Table Subscribe Multi-Events
    :subscribes({
        {
            event = "Event Class Name 1",
            handler = function(event)
                -- do something.
            end
        },
        {
            event = "Event Class Name 2",
            ignoreCancelled = true,
            handler = function(event)
                -- do something.
            end
        },
        {
            event = "Event Class Name 3",
            priority = "Event Priority",
            handler = function(event)
                -- do something.
            end
        }
    })
    :build()
    :register("Yours Subscribe Name")
```

## Try Let Player Cannot Break Blocks

Now that we know the full class name of [BlockBreakEvent](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockBreakEvent.html), 
and its Javadoc documentation, we can now implement how to prevent the player from breaking blocks.

```lua
-- Declare a variable called onBreakBlock and assign it a function.
-- This variable can now serve as our event handler.
local onBreakBlock = function (event)
    --  In order to prevent players from destroying blocks, we just need to cancel this event.
    event:setCancelled(true)

    -- At the same time, you have to remind the player what happened and why it can't be destroyed.
    local player = event:getPlayer()
    player:sendMessage("Hey, " .. player:getName() .. "! No break blocks allowed here!")
end

-- Okay, it's time to subscribe event and register to Bukkit.
luaBukkit.env:listenerBuilder()
    :subscribe({
        event = "org.bukkit.event.block.BlockBreakEvent",
        handler = onBreakBlock
    })
    :build()
    :register("NoBreak")
```

OK, now you can save the file, and then reload the Lua environment to see the effect in the game.

![NoBreak](./images/EventListener.NoBreakListener.png)

## Lua Side API

### LuaEventListenerProperty

`LuaEventListenerProperty` is used when passing parameters using Lua Table.
The passed Lua Table will be **automatically** converted to a `LuaEventListenerProperty` instance.

```java
/**
 * 事件监听JavaBean类型.
 */
public class LuaEventListenerProperty {
    /**
     * Event class name, required.
     */
    private String event;
    /**
     * Event priority, default is <code>NORMAL</code>, chooseable values:
     * <code>LOWEST</code>, <code>LOW</code>, <code>NORMAL</code>,
     * <code>HIGH</code>, <code>HIGHEST</code>, <code>MONITOR</code>.
     * And it is not case sensitive.
     */
    private String priority;
    /**
     * ignore canchelled event or not. default is false
     */
    private boolean ignoreCancelled = false;

    /**
     * Event handler, require.
     * This is a Lua function.
     */
    private ILuaCallable handler;
}
```

The Lua Table structure corresponding to this type is as follows:

```lua
local eventListener = {
    event = "",
    priority = "",
    ignoreCancelled = true,
    handler = function(event) end
}
```

tips: `handler` and `event` is required.

### ILuaEventListenerBuilder

```java
public interface ILuaEventListenerBuilder {
    /**
     * Subscribe a event
     *
     * @param eventClassName Event Class Name
     * @param closure        Lua Function
     * @return This Builder
     * @throws ClassNotFoundException If the event not exists then throws
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * Subscribe a event
     *
     * @param eventClassName Event Class Name
     * @param eventPriority  Event Priority
     * @param closure        Lua Function
     * @return This Builder
     * @throws ClassNotFoundException If the event not exists then throws
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull EventPriority eventPriority,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * Subscribe a event
     *
     * @param eventClassName Event Class Name
     * @param eventPriority  Event Priority, include <code>LOWEST</code> <code>LOW</code> <code>NORMAL</code>
     *                       <code>HIGH</code> <code>HIGHEST</code> <code>MONITOR</code>
     * @param closure        Lua Function
     * @return This Builder
     * @throws ClassNotFoundException If the event not exists then throws
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull String eventPriority,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * Subscribe a event
     *
     * @param eventClassName  Event Class Name
     * @param eventPriority   Event Priority
     * @param ignoreCancelled Ignore Cancelled .
     * @param closure         Lua Function
     * @return This Builder
     * @throws ClassNotFoundException If the event not exists then throws
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull EventPriority eventPriority,
                                       boolean ignoreCancelled,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * Subscribe a event
     *
     * @param eventClassName  Event Class Name
     * @param eventPriority   Event Priority, 包含<code>LOWEST</code> <code>LOW</code> <code>NORMAL</code>
     *                        <code>HIGH</code> <code>HIGHEST</code> <code>MONITOR</code>
     * @param ignoreCancelled Ignore Cancelled .
     * @param closure         Lua Function
     * @return This Builder
     * @throws ClassNotFoundException If the event not exists then throws
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       @NotNull String eventPriority,
                                       boolean ignoreCancelled,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * Subscribe a event
     *
     * @param eventClassName  Event Class Name
     * @param ignoreCancelled Ignore Cancelled .
     * @param closure         Lua Function
     * @return This Builder
     * @throws ClassNotFoundException If the event not exists then throws
     */
    ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                       boolean ignoreCancelled,
                                       @NotNull ILuaCallable closure) throws ClassNotFoundException;

    /**
     * Subscribe a event by LuaTable, Lua table must containts fields: <code>event</code>和<code>handler</code>.
     * <code>event</code> means event class name.
     * <code>handler</code> means lua function.
     *
     * @param table LuaTable
     * @return This Builder
     * @throws Exception if LuaTable do not containts requires fields
     */
    ILuaEventListenerBuilder subscribe(@NotNull LuaTable table) throws Exception;

    /**
     * 与<code>subscribe(LuaTable)</code>类似, 但是是接受一个LuaTable数组(数组风格LuaTable),
     * Subscribe batch events.
     *
     * @param tables table array, like<code>local tables = {nil, {}, {}, {}, nil}</code>
     * @return This Builder
     * @throws Exception if LuaTable do not containts requires fields
     */
    ILuaEventListenerBuilder subscribes(@NotNull LuaTable... tables) throws Exception;

    /**
     * Build Unregistered Listener
     *
     * @return Unregistered Listener
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
```

### LuaUnregisteredListener

This is the product constructed by the builder. As the name suggests, 
it has not been registered yet, so this type provides a registration method.

```java
/**
 * Unregistered Listener
 */
public class LuaUnregisteredListener {
    /**
     * Register to Bukkit
     * @param subscribeName Yours Subscribe Name.
     */
    public void register(String subscribeName);
}

```
