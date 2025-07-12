package org.eu.smileyik.luaInMinecraftBukkitII.jniBridge;

import org.eu.smileyik.luaInMinecraftBukkitII.NativeModule;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class JNIBridge implements NativeModule {
    public static final String MODULE_NAME = "jni-bridge";
    private final AtomicBoolean initialized = new AtomicBoolean(false);

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
