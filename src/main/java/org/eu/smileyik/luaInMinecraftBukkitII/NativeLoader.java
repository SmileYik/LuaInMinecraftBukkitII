package org.eu.smileyik.luaInMinecraftBukkitII;

import com.google.gson.Gson;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.config.NativeLibraryConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Objects;

public class NativeLoader {
    public static final String OS_LINUX = "linux";
    public static final String OS_WINDOWS = "windows";
    public static final String OS_MACOS = "macos";
    public static final String OS_OTHERS = "others";

    public static final String ARCH_X64 = "amd64";
    public static final String ARCH_ARM64 = "arm64";
    public static final String ARCH_OTHERS = "others";

    public static final String OS, ARCH;

    private static final int BUFFER_SIZE = 4096;
    private static final String VERSION_FILE = "VERSION";

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String os = OS_OTHERS;
        if (osName.contains("windows")) {
            os = OS_WINDOWS;
        } else if (osName.contains("mac os x") || osName.contains("darwin")) {
            os = OS_MACOS;
        } else if (osName.contains("linux")) {
            os = OS_LINUX;
        }
        OS = os;

        String arch = ARCH_OTHERS;
        if (osArch.contains("amd64")) {
            arch = ARCH_X64;
        } else if (osArch.contains("aarch64")) {
            arch = ARCH_ARM64;
        }
        ARCH = arch;
    }

    public static void load(Config config) throws IOException {
        String baseUrl = config.getProjectUrl();
        String luaVer = config.getLuaVersion();
        File baseDir = LuaInMinecraftBukkit.instance().getDataFolder();
        File nativeFolder = new File(baseDir, LuaInMinecraftBukkit.NATIVES_FOLDER);
        File versionFile = new File(nativeFolder, VERSION_FILE);
        String currentVersion = null;
        if (versionFile.exists()) {
            currentVersion = new String(Files.readAllBytes(versionFile.toPath()));
        }

        NativeLibraryConfig nativeConfig = getNativeConfig(baseUrl, nativeFolder);
        String[] files = nativeConfig.version(OS, ARCH, luaVer);
        if (files == null) {
            throw new RuntimeException("Sorry, this plugin is not supported on this platform");
        }
        if (!Objects.equals(luaVer, currentVersion)) {
            clean(nativeFolder);
            Files.write(versionFile.toPath(), luaVer.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        }

        for (String file : files) {
            File lib = new File(nativeFolder, file);
            checkFile(lib, config, nativeConfig);
            System.load(lib.getAbsolutePath());
        }
    }

    private static void clean(File nativeFolder) {
        if (!nativeFolder.exists()) {
            return;
        }
        File[] files = nativeFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".dll") ||
                        fileName.endsWith(".so") ||
                        fileName.endsWith(".dylib")
                ) {
                    file.delete();
                }
            }
        }
    }

    private static NativeLibraryConfig getNativeConfig(String baseUrl, File nativeFolder) throws IOException {
        File nativeConfigFile = new File(nativeFolder, "natives.json");
        if (!nativeConfigFile.exists()) {
            downloadLibraryConfig(baseUrl, nativeConfigFile);
        }
        return new Gson()
                .fromJson(new FileReader(nativeConfigFile), NativeLibraryConfig.class);
    }

    private static void downloadLibraryConfig(String baseUrl, File nativeConfigFile) {
        baseUrl += "/natives/natives.json";
        LuaInMinecraftBukkit.instance()
                .getLogger()
                .info(String.format(
                        "Not found natives.json, download from %s",
                        baseUrl
                ));
        try {
            byte[] bytes = downloadFile(baseUrl);
            Files.write(nativeConfigFile.toPath(), bytes,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Failed downloads natives config: " + baseUrl, e);
        }
    }

    private static byte[] downloadFile(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                BufferedOutputStream bos = new BufferedOutputStream(baos);
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
            return baos.toByteArray();
        }
    }

    private static void checkFile(File lib, Config config, NativeLibraryConfig libraryConfig) {
        if (!lib.exists() || config.isAlwaysCheckHashes()) {
            for (String baseUrl : libraryConfig.getUrls()) {
                baseUrl = String.join("/", baseUrl, OS, ARCH, config.getLuaVersion());
                String fileUrl = baseUrl + "/" + lib.getName();
                String hashUrl = fileUrl + ".hash";
                try {
                    int retry = 3;
                    while (retry > 0) {
                        if (!lib.exists()) {
                            LuaInMinecraftBukkit.instance()
                                    .getLogger()
                                    .info(String.format(
                                            "Not found library: %s, downloading library from %s",
                                            lib.getName(), fileUrl
                                    ));
                            byte[] fileBytes = downloadFile(fileUrl);
                            Files.write(lib.toPath(), fileBytes,
                                    StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.WRITE);
                        }
                        LuaInMinecraftBukkit.instance()
                                .getLogger()
                                .info("Downloading library hash file from " + hashUrl);
                        String targetHash = new String(downloadFile(hashUrl));
                        String downloadedHash = sha256(lib);
                        if (Objects.equals(downloadedHash, targetHash)) {
                            break;
                        }
                        LuaInMinecraftBukkit.instance()
                                .getLogger()
                                .info(String.format(
                                        "The file failed hash verification. " +
                                                "The current file hash value is %s. " +
                                                "The expected hash value is %s.",
                                        downloadedHash, targetHash
                                ));
                        lib.delete();
                        retry -= 1;
                    }
                    if (retry == 0) {
                        LuaInMinecraftBukkit.instance().getLogger().warning(
                                "The hash of library file is not correct: " + lib
                        );
                    } else {
                        break;
                    }
                } catch (Exception ignore) {

                }
            }
        }
        if (!lib.exists()) {
            throw new RuntimeException("Couldn't find library file: " + lib);
        }
    }

    public static String sha256(File file) throws NoSuchAlgorithmException, IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return sha256(bytes);
    }

    public static String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(bytes, 0, bytes.length);
        byte[] hashedBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

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
