package org.eu.smileyik.luaInMinecraftBukkitII.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static String sha256(File file) throws NoSuchAlgorithmException, IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return sha256(bytes);
    }

    public static String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(bytes, 0, bytes.length);
        byte[] hashedBytes = digest.digest();
        return HexUtil.bytesToHex(hashedBytes);
    }

    public static boolean isEqualsHashString(String p, String q) {
        return p != null && p.equalsIgnoreCase(q);
    }
}
