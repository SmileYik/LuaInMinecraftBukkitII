package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuaStateConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandClassBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandRegister;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.event.LuaEventListener;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaStateFacade;
import org.keplerproject.luajava.LuaStateFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LuaStateEnv implements AutoCloseable {

    static {
        File folder = new File(
                LuaInMinecraftBukkit.instance().getDataFolder(),
                LuaInMinecraftBukkit.NATIVES_FOLDER);
        if (!folder.exists()) {
            throw new UnsupportedOperationException("Not found natives libraries");
        }
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            System.load(file.getAbsolutePath());
        }
    }

    @Getter
    private final String id;
    @Getter
    private final LuaStateConfig config;
    private final LuaStateFacade lua;
    @Getter
    private final File rootDir;

    private final Map<String, Listener> listeners = new HashMap<>();
    private final Map<String, CommandService> commandServices = new HashMap<>();

    public LuaStateEnv(String id, LuaStateConfig config) {
        this.id = id;
        this.config = config;
        this.lua = LuaStateFactory.newLuaState(!this.config.isIgnoreAccessLimit());
        this.rootDir = new File(LuaInMinecraftBukkit.instance().getLuaStateFolder(), this.config.getRootDir());
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }

    public void initialization() {
        lua.openLibs();
        lua.getGlobal("package", LuaTable.class)
                .mapResultValue(table -> {
                    return table.get("cpath")
                            .mapValue(path -> (path) +
                                    ";" + rootDir.getAbsolutePath() + "/?.so" +
                                    ";" + rootDir.getAbsoluteFile() + "/?.dll"
                            )
                            .mapResultValue(cpath -> table.put("cpath", cpath))
                            .mapResultValue(it -> {
                                return table.get("path")
                                        .mapValue(path -> (path) +
                                                ";" + rootDir.getAbsolutePath() + "/?.lua"
                                        )
                                        .mapResultValue(path -> table.put("path", path));
                            });
                })
                .ifFailureThen(it -> {
                    DebugLogger.debug(DebugLogger.WARN,
                            "Error initializing lua package path: %s", it.getMessage());
                    DebugLogger.debug(DebugLogger.ERROR, it);
                });

        lua.newTable();
        lua.toJavaObject(-1)
                .mapResultValue(obj -> {
                    LuaTable table = ((LuaTable) obj).asTable();
                    Result<Void, ? extends LuaException> result = table.put("env", this)
                            .mapResultValue(it -> table.put("plugin", LuaInMinecraftBukkit.instance()))
                            .mapResultValue(it -> table.put("bukkit", Bukkit.class))
                            .mapResultValue(it -> table.put("server", LuaInMinecraftBukkit.instance().getServer()));
                    return result.isError() ? result.justCast() : Result.success(table);
                })
                .mapResultValue(table -> lua.setGlobal("luaBukkit", table))
                .ifFailureThen(err -> {
                    DebugLogger.debug(DebugLogger.WARN,
                            "Error initializing global variable 'luaBukkit': %s", err.getMessage());
                    DebugLogger.debug(DebugLogger.ERROR, err);
                });

        for (String file : config.getInitialization()) {
            File scripFile = new File(rootDir, file);
            String absolutePath = scripFile.getAbsolutePath();
            if (scripFile.exists()) {
                lua.evalFile(absolutePath)
                        .ifFailureThen(err -> {
                            LuaInMinecraftBukkit.instance()
                                    .getLogger()
                                    .warning(String.format(
                                            "Failed to eval lua file '%s', because: %s", file, err.getMessage()));
                            DebugLogger.debug(DebugLogger.WARN,
                                    "Failed to eval lua file '%s', because: %s", file, err.getMessage());
                            DebugLogger.debug(DebugLogger.ERROR, err);
                        });
            } else {
                LuaInMinecraftBukkit.instance()
                        .getLogger()
                        .warning(String.format("Cannot find file: %s", file));
            }
        }
    }

    public void registerEventListener(String name, Listener listener) {
        Listener oldOne = listeners.put(name, listener);
        LuaInMinecraftBukkit.instance()
                .getServer()
                .getPluginManager()
                .registerEvents(listener, LuaInMinecraftBukkit.instance());
        if (oldOne != null) {
            HandlerList.unregisterAll(oldOne);
        }
    }

    public LuaEventBuilder eventBuilder() {
        return new LuaEventBuilder(this);
    }

    public LuaCommandClassBuilder newCommandClassBuilder() {
        return new LuaCommandClassBuilder();
    }

    public Result<Boolean, Exception> registerCommand(String rootCommand, Class<?> ... classes) {
        try {
            CommandService commandService = LuaCommandRegister.register(rootCommand, classes);
            if (commandService == null) {
                return Result.success(false);
            }
            commandServices.put(rootCommand, commandService);
            commandService.registerToBukkit(LuaInMinecraftBukkit.instance());
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public String path(String path) {
        return file(path).toString();
    }

    public String path(String ... paths) {
        return file(paths).toString();
    }

    public File file(String path) {
        return new File(this.rootDir, path);
    }

    public File file(String ... paths) {
        return new File(rootDir, String.join(File.pathSeparator, paths));
    }

    @Override
    public void close() {
        listeners.forEach((name, listener) -> {
            HandlerList.unregisterAll(listener);
            if (listener instanceof LuaEventListener) {
                ((LuaEventListener) listener).clear();
            }
        });
        listeners.clear();

        commandServices.forEach((name, commandService) -> {
            commandService.shutdown();
        });
        commandServices.clear();

        if (lua != null) {
            lua.close();
        }
    }
}
