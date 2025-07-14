package org.eu.smileyik.luaInMinecraftBukkitII.module.jniBridge;

import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.module.NativeModule;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JNIBridge 是一个反射工具, 用于 Cpp 与 Java 交互.
 */
public class JNIBridge implements NativeModule {
    public static final String MODULE_NAME = "jni-bridge";
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public File baseDir() {
        return new File(
                LuaInMinecraftBukkit.instance().getDataFolder(),
                LuaInMinecraftBukkit.NATIVES_FOLDER
        );
    }

    /**
     * check JNIBridge is initialized or not.
     * @return true if initialized.
     */
    @Override
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * initialize JNIBridge.
     * @param libPaths shared library path.
     */
    @Override
    public void initialize(LinkedList<String> libPaths) {
        if (initialized.compareAndSet(false, true)) {
            libPaths.forEach(System::load);
            initBridge();
        }
    }

    public static native void initBridge();
}
