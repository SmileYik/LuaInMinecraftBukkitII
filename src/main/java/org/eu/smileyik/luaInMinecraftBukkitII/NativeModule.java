package org.eu.smileyik.luaInMinecraftBukkitII;

import java.util.LinkedList;

public interface NativeModule {
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
