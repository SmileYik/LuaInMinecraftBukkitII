package org.eu.smileyik.luaInMinecraftBukkitII.luaState.command;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.CommandProperties;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.ILuaCommandBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.ILuaCommandClassBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.reflect.LuaTable2Object;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simplecommand.annotation.Command;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 指令类构造器.
 */
public class LuaCommandClassBuilder implements ILuaCommandClassBuilder {
    private final List<CommandConfig> configs = new LinkedList<>();
    private String[] _aliases = null;
    private String _description = null;
    private String _permission = null;
    private boolean _needPlayer = false;

    @Override
    public ILuaCommandBuilder command(@NotNull String commandName) {
        return new LuaCommandBuilder(this, commandName);
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull LuaTable table) throws Exception {
        CommandProperties properties = LuaTable2Object.covert(table, CommandProperties.class)
                .getOrThrow();
        if (properties.getCommand() == null) {
            throw new IllegalArgumentException("command field must be set in table!");
        }
        if (properties.getHandler() == null) {
            throw new IllegalArgumentException("handler field must be set in table!");
        }
        return command(
                properties.getHandler(),
                properties.getCommand(),
                properties.getArgs(),
                properties.getDescription(),
                properties.getPermission(),
                properties.isNeedPlayer(),
                properties.isUnlimitedArgs()
        );
    }

    @Override
    public ILuaCommandClassBuilder commands(@NotNull LuaTable... tables) throws Exception {
        for (LuaTable table : tables) {
            command(table);
        }
        return this;
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command) {
        return command(callable, command, new String[0], "", "", false, false);
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable,
                                           @NotNull String command,
                                           @NotNull String description) {
        return command(callable, command, new String[0], description, "", false, false);
    }


    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args) {
        return command(callable, command, args, "", "", false, false);
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                           String description) {
        return command(callable, command, args, description, "", false, false);
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                           String description, String permission) {
        return command(callable, command, args, description, permission, false, false);
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                           String description, String permission, boolean needPlayer) {
        return command(callable, command, args, description, permission, needPlayer, false);
    }

    @Override
    public ILuaCommandClassBuilder command(@NotNull ILuaCallable callable,
                                           @NotNull String command,
                                           String[] args, String description, String permission,
                                           boolean needPlayer, boolean unlimitedArgs) {
        if (args == null) {
            args = new String[0];
        }
        if (description == null) {
            description = "";
        }
        if (permission == null) {
            permission = "";
        }
        this.configs.add(new CommandConfig(
                callable, command, args, description, permission, needPlayer, unlimitedArgs));
        return this;
    }

    @Override
    public ILuaCommandClassBuilder aliases(String... aliases) {
        this._aliases = aliases;
        return this;
    }

    @Override
    public ILuaCommandClassBuilder needPlayer() {
        this._needPlayer = true;
        return this;
    }

    @Override
    public ILuaCommandClassBuilder description(String description) {
        this._description = description;
        return this;
    }

    @Override
    public ILuaCommandClassBuilder permission(String permission) {
        this._permission = permission;
        return this;
    }

    @Override
    public Class<?> build(LuaTable metaTable, LuaTable... commandTables) throws Exception {
        CommandProperties properties = LuaTable2Object.covert(metaTable, CommandProperties.class)
                .getOrThrow();

        if (properties.getCommand() == null) {
            throw new IllegalArgumentException("command field must be set in table!");
        }
        this._aliases = properties.getAliases();
        this._description = properties.getDescription();
        this._permission = properties.getPermission();
        this._needPlayer = properties.isNeedPlayer();
        commands(commandTables);
        return build(properties.getCommand(), properties.getParentCommand());
    }

    @Override
    public Class<?> build(@NotNull String command) {
        return build(command, null);
    }

    @Override
    public Class<?> build(@NotNull String command, String parentCommand) {
        if (parentCommand == null) {
            parentCommand = "";
        }
        if (_aliases == null) {
            _aliases = new String[0];
        }
        if (_description == null) {
            _description = "";
        }
        if (_permission == null) {
            _permission = "";
        }

        AnnotationDescription annoClass = AnnotationDescription.Builder.ofType(Command.class)
                .define("value", command)
                .define("parentCommand", parentCommand)
                .defineArray("aliases", _aliases)
                .define("description", _description)
                .define("permission", _permission)
                .define("needPlayer", _needPlayer)
                .build();
        DynamicType.Builder<Object> byteBuddy = new ByteBuddy()
                .subclass(Object.class)
                .annotateType(annoClass);

        int count = 0;
        Map<String, ILuaCallable> callables = new HashMap<>();
        LuaCommandHandler commandHandler = new LuaCommandHandler(callables);
        for (CommandConfig config : configs) {
            AnnotationDescription annoMethod = AnnotationDescription.Builder.ofType(Command.class)
                    .define("value", config.command)
                    .define("description", config.description)
                    .define("permission", config.permission)
                    .define("needPlayer", config.needPlayer)
                    .defineArray("args", config.args)
                    .define("isUnlimitedArgs", config.unlimitedArgs)
                    .build();

            String methodName = String.format("onCommand%d", count++);
            callables.put(methodName, config.callable);
            byteBuddy = byteBuddy.defineMethod(methodName, Void.class, Visibility.PUBLIC)
                    .withParameters(CommandSender.class, String[].class)
                    .intercept(MethodDelegation.to(commandHandler))
                    .annotateMethod(annoMethod);
        }

        try (DynamicType.Unloaded<Object> made = byteBuddy.make()) {
            return made.load(LuaInMinecraftBukkit.instance()
                            .getClass()
                            .getClassLoader())
                    .getLoaded();
        } catch (Exception ee) {
            throw new RuntimeException(ee);
        } finally {
            configs.clear();
        }


    }

    private static class CommandConfig {
        private final ILuaCallable callable;
        private final String command;
        private final String[] args;
        private final String description;
        private final String permission;
        private final boolean needPlayer;
        private final boolean unlimitedArgs;

        private CommandConfig(ILuaCallable callable, String command, String[] args,
                              String description, String permission, boolean needPlayer, boolean unlimitedArgs) {
            this.callable = callable;
            this.command = command;
            this.args = args;
            this.description = description;
            this.permission = permission;
            this.needPlayer = needPlayer;
            this.unlimitedArgs = unlimitedArgs;
        }
    }
}
