package org.eu.smileyik.luaInMinecraftBukkitII.util;

import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class ResourcesExtractor {

    public static void extractResources(String resourceName, File targetDir) {
        Class<ResourcesExtractor> clazz = ResourcesExtractor.class;
        try (InputStream listIs = clazz.getResourceAsStream("/resources/" + resourceName + ".list")) {
            if (listIs == null) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(listIs))) {
                reader.lines().forEach(fileName -> {
                    File file = new File(targetDir, fileName);
                    if (file.exists()) {
                        return;
                    }
                    File parentFile = file.getParentFile();
                    if (parentFile != null &&
                            !parentFile.exists() &&
                            !parentFile.mkdirs()) {
                        return;
                    }

                    try (
                            InputStream is = clazz.getResourceAsStream("/resources/" + resourceName + "/" + fileName);
                            BufferedInputStream in = new BufferedInputStream(Objects.requireNonNull(is));
                            BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()));
                    ) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            DebugLogger.debug(DebugLogger.ERROR,
                    "Failed to extract resources '%s' to '%s'",  resourceName, targetDir);
            DebugLogger.debug(e);
        }
    }
}
