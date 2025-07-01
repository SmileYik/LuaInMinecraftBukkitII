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
    private String[] _args = null;
    private String _description = null;
    private String _permission = null;
    private boolean _needPlayer = false;
    private boolean _unlimitedArgs = false;

    protected LuaCommandBuilder(ILuaCommandClassBuilder builder, String command) {
        this.builder = builder;
        this.command = command;
    }

    @Override
    public ILuaCommandBuilder args(String... args) {
        this._args = args;
        return this;
    }

    @Override
    public ILuaCommandBuilder desc(String description) {
        this._description = description;
        return this;
    }

    @Override
    public ILuaCommandBuilder description(String description) {
        this._description = description;
        return this;
    }

    @Override
    public ILuaCommandBuilder permission(String permission) {
        this._permission = permission;
        return this;
    }

    @Override
    public ILuaCommandBuilder needPlayer() {
        this._needPlayer = true;
        return this;
    }

    @Override
    public ILuaCommandBuilder unlimitedArgs() {
        this._unlimitedArgs = true;
        return this;
    }

    @Override
    public ILuaCommandClassBuilder handler(ILuaCallable callable) {
        builder.command(callable, command,
                _args, _description, _permission,
                _needPlayer, _unlimitedArgs);
        return builder;
    }
}
