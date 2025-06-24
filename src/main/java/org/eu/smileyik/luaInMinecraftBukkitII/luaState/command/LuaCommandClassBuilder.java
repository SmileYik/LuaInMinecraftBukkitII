package org.eu.smileyik.luaInMinecraftBukkitII.luaState.command;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.simplecommand.annotation.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LuaCommandClassBuilder {
    private final List<CommandConfig> configs = new LinkedList<>();

    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command) {
        return command(callable, command, new String[0], "", "", false, false);
    }

    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable,
                                          @NotNull String command,
                                          @NotNull String description) {
        return command(callable, command, new String[0], description, "", false, false);
    }


    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args) {
        return command(callable, command, args, "", "", false, false);
    }

    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                          String description) {
        return command(callable, command, args, description, "", false, false);
    }

    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                          String description, String permission) {
        return command(callable, command, args, description, permission, false, false);
    }

    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                          String description, String permission, boolean needPlayer) {
        return command(callable, command, args, description, permission, needPlayer, false);
    }

    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable,
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

    public Class<?> build(@NotNull String topCommand) {
        return build(topCommand, "", new String[0], "", "", false);
    }

    public Class<?> build(@NotNull String topCommand, @Nullable String[] aliases) {
        return build(topCommand, "", aliases, "", "", false);
    }

    public Class<?> build(@NotNull String topCommand,
                          @Nullable String parentCommand,
                          @Nullable String description) {
        return build(topCommand, parentCommand, new String[0], description, "", false);
    }

    public Class<?> build(@NotNull String topCommand, @Nullable String parentCommand,
                          @Nullable String description, @Nullable String permission) {
        return build(topCommand, parentCommand, new String[0], description, permission, false);
    }

    public Class<?> build(@NotNull String topCommand,
                          @Nullable String[] aliases,
                          @Nullable String description) {
        return build(topCommand, "", aliases, description, "", false);
    }

    public Class<?> build(@NotNull String topCommand, @Nullable String parentCommand,
                          @Nullable String[] aliases, @Nullable String description,
                          @Nullable String permission) {
        return build(topCommand, parentCommand, aliases, description, permission, false);
    }

    public Class<?> build(@NotNull String topCommand, @Nullable String parentCommand,
                          @Nullable String[] aliases, @Nullable String description,
                          @Nullable String permission, boolean needPlayer) {
        if (parentCommand == null) {
            parentCommand = "";
        }
        if (aliases == null) {
            aliases = new String[0];
        }
        if (description == null) {
            description = "";
        }
        if (permission == null) {
            permission = "";
        }

        AnnotationDescription annoClass = AnnotationDescription.Builder.ofType(Command.class)
                .define("value", topCommand)
                .define("parentCommand", parentCommand)
                .defineArray("aliases", aliases)
                .define("description", description)
                .define("permission", permission)
                .define("needPlayer", needPlayer)
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
