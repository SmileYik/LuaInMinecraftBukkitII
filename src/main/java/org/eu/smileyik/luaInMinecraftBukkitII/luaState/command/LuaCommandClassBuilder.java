package org.eu.smileyik.luaInMinecraftBukkitII.luaState.command;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
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
public class LuaCommandClassBuilder {
    private final List<CommandConfig> configs = new LinkedList<>();
    private String[] aliases = null;
    private String description = null;
    private String permission = null;
    private boolean needPlayer = false;

    /**
     * 使用指令构造器新建一个指令. 对于它的其他方法重载来讲, 该方法可能更加优雅.
     * @param commandName 指令名称
     * @return 指令构造器
     */
    public LuaCommandBuilder command(@NotNull String commandName) {
        return new LuaCommandBuilder(this, commandName);
    }

    /**
     * 注册一个指令.
     * table格式详细请看<code>CommandProperties</code>类
     * @param table table, table 必须包含 <code>command</code> 与 <code>handler</code> 字段
     * @return 构造器
     * @throws Exception 如果给予的table不符合规范时抛出.
     * @see CommandProperties
     */
    public LuaCommandClassBuilder command(@NotNull LuaTable table) throws Exception {
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

    /**
     * 添加若干数量指令
     * @param tables lua table 数组
     * @return 此构造器
     * @throws Exception 如果给予的table不符合规范时抛出.
     * @see CommandProperties
     * @see LuaCommandClassBuilder#command(LuaTable)
     */
    public LuaCommandClassBuilder commands(@NotNull LuaTable ... tables) throws Exception {
        for (LuaTable table : tables) {
            command(table);
        }
        return this;
    }

    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @return 此构造器
     */
    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command) {
        return command(callable, command, new String[0], "", "", false, false);
    }

    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @param description 指令描述
     * @return 此构造器
     */
    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable,
                                          @NotNull String command,
                                          @NotNull String description) {
        return command(callable, command, new String[0], description, "", false, false);
    }


    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @param args     指令参数
     * @return 此构造器
     */
    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args) {
        return command(callable, command, args, "", "", false, false);
    }

    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @param args     指令参数
     * @param description 指令描述
     * @return 此构造器
     */
    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                          String description) {
        return command(callable, command, args, description, "", false, false);
    }

    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @param args     指令参数
     * @param description 指令描述
     * @param permission 指令权限
     * @return 此构造器
     */
    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                          String description, String permission) {
        return command(callable, command, args, description, permission, false, false);
    }

    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @param args     指令参数
     * @param description 指令描述
     * @param permission 指令权限
     * @param needPlayer 是否需要玩家执行
     * @return 此构造器
     */
    public LuaCommandClassBuilder command(@NotNull ILuaCallable callable, @NotNull String command, String[] args,
                                          String description, String permission, boolean needPlayer) {
        return command(callable, command, args, description, permission, needPlayer, false);
    }

    /**
     * 添加一个指令.
     * @param callable lua闭包
     * @param command  指令名称
     * @param args     指令参数
     * @param description 指令描述
     * @param permission 指令权限
     * @param needPlayer 是否需要玩家执行
     * @param unlimitedArgs 是否无限参数长度
     * @return 此构造器
     */
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

    /**
     * 设定指令别名, 当且仅当该指令为顶级指令时有效
     * @param aliases 别名
     * @return 构造器
     */
    public LuaCommandClassBuilder aliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * 设置该类中所有指令都需要玩家才能执行.
     * @return 该构造器
     */
    public LuaCommandClassBuilder needPlayer() {
        this.needPlayer = true;
        return this;
    }

    /**
     * 设置该指令描述
     * @param description 描述
     * @return 构造器
     */
    public LuaCommandClassBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * 设置该类下所有指令所需要的权限.
     * @param permission 权限
     * @return 构造器
     */
    public LuaCommandClassBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * 构建指令类型.
     * @param metaTable     lua table,
     *                      有效字段为 command(必填),
     *                      aliases, description, permission, needPlayer, parentCommand
     * @param commandTables 与commands方法类似.
     * @return 指令类型
     * @throws Exception 如果不符合要求时抛出, 例如缺少必填字段
     * @see CommandProperties
     * @see LuaCommandClassBuilder#command(LuaTable)
     */
    public Class<?> build(LuaTable metaTable, LuaTable ... commandTables) throws Exception {
        CommandProperties properties = LuaTable2Object.covert(metaTable, CommandProperties.class)
                .getOrThrow();

        if (properties.getCommand() == null) {
            throw new IllegalArgumentException("command field must be set in table!");
        }
        this.aliases = properties.getAliases();
        this.description = properties.getDescription();
        this.permission = properties.getPermission();
        this.needPlayer = properties.isNeedPlayer();
        commands(commandTables);
        return build(properties.getCommand(), properties.getParentCommand());
    }

    public Class<?> build(@NotNull String command) {
        return build(command, null);
    }

    public Class<?> build(@NotNull String command, String parentCommand) {
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
                .define("value", command)
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
