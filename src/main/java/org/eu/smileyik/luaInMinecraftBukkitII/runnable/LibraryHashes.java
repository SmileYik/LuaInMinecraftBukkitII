package org.eu.smileyik.luaInMinecraftBukkitII.runnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import static org.eu.smileyik.luaInMinecraftBukkitII.util.HashUtil.sha256;

public class LibraryHashes {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        if (args.length == 0) return;
        File nativeFolder = new File(args[0]);
        if (!nativeFolder.exists()) {
            return;
        }

        LinkedList<File> files = new LinkedList<>();
        files.add(nativeFolder);
        while (!files.isEmpty()) {
            nativeFolder = files.remove();
            File[] subs = nativeFolder.listFiles();
            if (subs != null) {
                for (File sub : subs) {
                    if (sub.isDirectory()) {
                        files.add(sub);
                    } else if (!sub.getName().endsWith(".hash")) {
                        String path = sub.getAbsolutePath();
                        String sha256Path = path + ".hash";
                        String sha256 = sha256(sub);
                        Files.write(Paths.get(sha256Path), sha256.getBytes(),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE,
                                StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }
            }
        }
    }
}
