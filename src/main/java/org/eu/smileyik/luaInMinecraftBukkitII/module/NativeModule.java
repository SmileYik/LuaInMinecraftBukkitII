package org.eu.smileyik.luaInMinecraftBukkitII.module;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

import static org.eu.smileyik.luaInMinecraftBukkitII.module.NativeModuleRegister.modules;

public interface NativeModule {

    public static final Map<String, NativeModule> MODULES = modules();

    /**
     * the directory stored native modules.
     */
    File baseDir();

    /**
     * check module is initialized or not.
     * @return true if initialized.
     */
    boolean isInitialized();

    /**
     * initialize module.
     * @param libPaths libraries paths.
     */
    void initialize(LinkedList<String> libPaths);
}
