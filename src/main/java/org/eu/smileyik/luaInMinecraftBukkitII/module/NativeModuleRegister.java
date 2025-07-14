package org.eu.smileyik.luaInMinecraftBukkitII.module;

import org.eu.smileyik.luaInMinecraftBukkitII.module.cffi.CFFI;
import org.eu.smileyik.luaInMinecraftBukkitII.module.jniBridge.JNIBridge;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NativeModuleRegister {
    protected static Map<String, NativeModule> modules() {
        Map<String, NativeModule> modules = new HashMap<>();

        // register modules.
        modules.put(JNIBridge.MODULE_NAME, new JNIBridge());
        modules.put(CFFI.MODULE_NAME, new CFFI());

        return Collections.unmodifiableMap(modules);
    }
}
