package org.eu.smileyik.luaInMinecraftBukkitII;

public class JsonUtil {
    public static String stripComments(String input) {
        return input.replaceAll("[ \\t]+//.+\\n", "");
    }
}
