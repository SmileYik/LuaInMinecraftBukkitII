package org.eu.smileyik.luaInMinecraftBukkitII.luaState.command;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.reflect.ReflectUtil;
import org.eu.smileyik.simplecommand.CommandMessageFormat;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simplecommand.CommandTranslator;

import java.io.InvalidClassException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class LuaCommandRegister {
    public static final CommandTranslator DEFAULT_TRANSLATOR = (msg, obj) -> msg;
    public static final CommandMessageFormat DEFAULT_FORMAT = new CommandMessageFormat() {
        @Override
        public String notFound() {
            return "指令未找到!";
        }

        @Override
        public String notFound(String suggestCommandHelp) {
            return "指令未找到, 猜你可能想执行: \n" + suggestCommandHelp;
        }

        @Override
        public String commandError() {
            return "指令未找到!";
        }

        @Override
        public String commandError(String suggestCommandHelp) {
            return "指令未找到, 猜你可能想执行: \n" + suggestCommandHelp;
        }

        @Override
        public String notPlayer() {
            return "只能玩家执行这个指令!";
        }

        @Override
        public String notPermission() {
            return "指令不存在!";
        }
    };

    private static final Field COMMAND_MAP_FIELD;
    private static final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR;

    static {
        Server server = LuaInMinecraftBukkit.instance().getServer();
        Field commandMapField = ReflectUtil.findFieldByType(server.getClass(), CommandMap.class);
        if (commandMapField == null) {
            throw new RuntimeException("Could not find CommandMap");
        }
        commandMapField.setAccessible(true);
        COMMAND_MAP_FIELD = commandMapField;

        try {
            PLUGIN_COMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            PLUGIN_COMMAND_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find PluginCommand constructor", e);
        }
    }

    public static CommandService register(String rootCommand, String[] aliases, Class<?> ... classes)
            throws InvalidClassException,
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        PluginCommand pluginCommand = PLUGIN_COMMAND_CONSTRUCTOR.newInstance(rootCommand, plugin);
        if (aliases != null) {
            pluginCommand.setAliases(Arrays.asList(aliases));
        }
        CommandMap commandMap = (CommandMap) COMMAND_MAP_FIELD.get(plugin.getServer());
        if (commandMap.register(rootCommand, plugin.getName(), pluginCommand)) {
            return CommandService.newInstance(
                    DEFAULT_TRANSLATOR, DEFAULT_FORMAT,
                    classes
            );
        }

        return null;
    }
}
