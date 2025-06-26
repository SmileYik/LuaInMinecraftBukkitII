package org.eu.smileyik.luaInMinecraftBukkitII.luaState.command;

import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.ILuaCommandBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.command.ILuaCommandClassBuilder;
import org.eu.smileyik.luajava.type.ILuaCallable;

/**
 * 指令构造器.
 */
public class LuaCommandBuilder implements ILuaCommandBuilder {
    private final ILuaCommandClassBuilder builder;
    private final String command;
    private String[] args = null;
    private String description = null;
    private String permission = null;
    private boolean needPlayer = false;
    private boolean unlimitedArgs = false;

    protected LuaCommandBuilder(ILuaCommandClassBuilder builder, String command) {
        this.builder = builder;
        this.command = command;
    }

    @Override
    public ILuaCommandBuilder args(String... args) {
        this.args = args;
        return this;
    }

    @Override
    public ILuaCommandBuilder desc(String description) {
        this.description = description;
        return this;
    }

    @Override
    public ILuaCommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public ILuaCommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public ILuaCommandBuilder needPlayer() {
        this.needPlayer = true;
        return this;
    }

    @Override
    public ILuaCommandBuilder unlimitedArgs() {
        this.unlimitedArgs = true;
        return this;
    }

    @Override
    public ILuaCommandClassBuilder handler(ILuaCallable callable) {
        builder.command(callable, command, args, description, permission, needPlayer, unlimitedArgs);
        return builder;
    }
}
