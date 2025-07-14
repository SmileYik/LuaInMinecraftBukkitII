package org.eu.smileyik.luaInMinecraftBukkitII.module.cffi;

import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.module.NativeModule;

import java.io.File;
import java.util.LinkedList;

/**
 * CFFI 是一个 Lua 高版本的 luajit ffi 替代品, 其是预编译好的 cffi-lua 项目, 支持 lua 51 ~ 54.
 */
public class CFFI implements NativeModule {
    public static final String MODULE_NAME = "cffi";
    private boolean initialized = false;

    /**
     * the directory stored native modules.
     */
    @Override
    public File baseDir() {
        return new File(
                LuaInMinecraftBukkit.instance().getDataFolder(),
                LuaInMinecraftBukkit.LUA_LIB_FOLDER
        );
    }

    /**
     * check module is initialized or not.
     *
     * @return true if initialized.
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * initialize module.
     *
     * @param libPaths libraries paths.
     */
    @Override
    public void initialize(LinkedList<String> libPaths) {
        initialized = true;
    }
}
