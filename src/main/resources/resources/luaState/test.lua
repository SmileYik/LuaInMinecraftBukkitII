-- file location: /plugins/LuaInMinecraftBukkitII/luastate/default/test.lua
local monitorTask = nil

-- define command tables, will use it to build command class.
local commands = {
    {
        command = "hello",
        description = "Say Hello to you",
        args = {"msg"},
        handler = function (sender, args)
            sender:sendMessage("Hello, " .. args[1] .. "!")
        end
    },
    {
        command = "hi",
        description = "Say Hi to you",
        needPlayer = true,
        handler = function (sender, args)
            sender:sendMessage("Hello, " .. sender:getName() .. "!")
        end
    }
}

-- build a command class
local topCommandClass = luaBukkit.env:commandClassBuilder()
    :commands(commands)
    :command("say")
        :args({"msg"})
        :description("say something")
        :handler(function(sender, args)
            sender:sendMessage(args[1])
        end)
    :aliases({"tc", "testc"})
    :build("TestCommand")



-- register command class
-- after register, you can use "/TestCommand" or aliases "/tc", "/testc" to run command.
-- also you can use "/TestCommand help" to display command help information.
local result = luaBukkit.env:registerCommand("TestCommand", {"tc", "testc"}, {topCommandClass})
if result:isError() then
    luaBukkit.log:info("Register command failed!")
else
    luaBukkit.log:info("Register command successed! enter '/TestCommand help' to show help!")
end

-- define listeners
local listeners = {
    {
        event = "PlayerJoinEvent",
        handler = function(event)
            luaBukkit.log:info("event priority normal")
        end
    },
    {
        event = "org.bukkit.event.player.PlayerJoinEvent",
        priority = "HIGH",
        handler = function(event)
            luaBukkit.log:info("event priority high")
        end
    }
}

-- build listeners
luaBukkit.env:listenerBuilder()
    :subscribes(listeners)
    :subscribe({
        event = "PlayerJoinEvent",
        priority = "LOW",
        handler = function(event)
            luaBukkit.log:info("event low")
            if monitorTask ~= nil then
                monitorTask:cancel()
            end
        end
    })
    :build()
    :register("MyListeners")

-- global function variable, can be called by command
-- execute command in game: /lua call default test
function test()
    luaBukkit.log:info("Test")
end

monitorTask = luaBukkit.helper:asyncTimer(
    function()
        luaBukkit.log:info("Still not player join sever...?")
    end, 0, 20, nil
)

luaBukkit.helper:runnable(function()
    luaBukkit.log:info("I am a runnable")
end):run()

local function sayHelloByLogger()
    luaBukkit.log:info("test.lua script is finished initialization!")
end

local function sayHelloByMessage()
    local import = require("import")
    local MiniMessage = import "net.kyori.adventure.text.minimessage.MiniMessage"
    local component = MiniMessage:miniMessage():deserialize("<rainbow>test.lua script is finished initialization!")
    luaBukkit.server:sendMessage(component)
end

local function sayHello()
    local success = pcall(sayHelloByMessage)
    if not success then
        sayHelloByLogger()
    end
end

sayHello()