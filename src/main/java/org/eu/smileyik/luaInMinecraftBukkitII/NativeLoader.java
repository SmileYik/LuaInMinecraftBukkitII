package org.eu.smileyik.luaInMinecraftBukkitII;

import com.google.gson.Gson;
import org.bukkit.plugin.Plugin;
import org.eu.smileyik.luaInMinecraftBukkitII.config.Config;
import org.eu.smileyik.luaInMinecraftBukkitII.config.NativeLibraryConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.module.NativeModule;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NativeLoader {
    public static final String OS_LINUX = "linux";
    public static final String OS_WINDOWS = "windows";
    public static final String OS_MACOS = "macos";
    public static final String OS_OTHERS = "others";

    public static final String ARCH_X64 = "amd64";
    public static final String ARCH_ARM64 = "arm64";
    public static final String ARCH_OTHERS = "others";

    public static final String[] DYNAMIC_FILE_TYPE_LINUX = new String[] { ".so" };
    public static final String[] DYNAMIC_FILE_TYPE_WINDOWS = new String[] { ".dll" };
    public static final String[] DYNAMIC_FILE_TYPE_MACOS = new String[] { ".dylib", ".so" };

    public static final String OS, ARCH;

    private static final int BUFFER_SIZE = 4096;
    private static final String VERSION_FILE = "VERSION";
    private static final String FALLBACK_BASE_URL = "https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/heads/gh-page";

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

    /**
     * check plugin version. if version not match then will delete natives folder
     */
    private static void pluginVersionCheck(Plugin instance) {
        if (!(instance instanceof LuaInMinecraftBukkit)) {
            return;
        }
        String version = ((LuaInMinecraftBukkit) instance).version();
        // version show `master` means it's not a release version, just skip check.
        if (version.startsWith("master")) {
            return;
        }
        File file = new File(instance.getDataFolder(), VERSION_FILE);
        boolean needUpdateNatives = true;
        try {
            if (file.exists()) {
                String prevVersion = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                needUpdateNatives = !Objects.equals(prevVersion, version);
            }
            if (needUpdateNatives) {
                File nativeFolder = new File(instance.getDataFolder(), LuaInMinecraftBukkit.NATIVES_FOLDER);
                File[] files = nativeFolder.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            f.delete();
                        }
                    }
                }
                Files.write(file.toPath(), version.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
                instance.getLogger().info("Native library version need updated, currently: " + version);
            }
        } catch (Exception e) {
            instance.getLogger().warning("Failed to check plugin native version: " + e);
        }
    }

    public static void load(Plugin plugin, Config config) throws IOException {
        pluginVersionCheck(plugin);
        String baseUrl = config.getProjectUrl();
        String luaVer = config.getLuaVersion();
        File baseDir = plugin.getDataFolder();
        File nativeFolder = new File(baseDir, LuaInMinecraftBukkit.NATIVES_FOLDER);
        File versionFile = new File(nativeFolder, VERSION_FILE);
        String currentVersion = null;
        if (versionFile.exists()) {
            currentVersion = new String(Files.readAllBytes(versionFile.toPath()));
        }

        NativeLibraryConfig nativeConfig = null;
        try {
            nativeConfig = getNativeConfig(plugin, baseUrl, nativeFolder);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to download natives.json, try again: " + e.getMessage());
            baseUrl = FALLBACK_BASE_URL;
            nativeConfig = getNativeConfig(plugin, baseUrl, nativeFolder);
        }
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
            checkFile(plugin, lib, config, nativeConfig, config.getLuaVersion());
            System.load(lib.getAbsolutePath());
        }

        // load modules
        Collection<String> availableModules = nativeConfig.availableModules(OS, ARCH);
        for (String module : config.getEnableModules()) {
            NativeModule nativeModule = NativeModule.MODULES.get(module);
            if (nativeModule == null) {
                plugin.getLogger().warning(String.format(
                        "Skipping module '%s' because not found.", module));
                continue;
            }

            // find real module name.
            // sometimes module depends on lua version.
            // if depends on it then the real name should be "module-luaVersion"
            String realModule = null;
            if (availableModules.contains(module)) {
                realModule = module;
            } else if (availableModules.contains(module + "-" + config.getLuaVersion())) {
                realModule = module + "-" + config.getLuaVersion();
            } else {
                plugin.getLogger().warning(String.format(
                        "Skipping module '%s' because not available.", module));
                continue;
            }

            if (!nativeModule.isInitialized()) {
                try {
                    File nativeBaseDir = nativeModule.baseDir();

                    String[] moduleFiles = nativeConfig.module(OS, ARCH, realModule);
                    LinkedList<String> paths = new LinkedList<>();
                    if (moduleFiles != null) {
                        for (String file : moduleFiles) {
                            if (file != null) {
                                File lib = new File(nativeBaseDir, file);
                                checkFile(plugin, lib, config, nativeConfig, realModule);
                                paths.add(lib.getAbsolutePath());
                            }
                        }
                    }
                    nativeModule.initialize(paths);
                } catch (Exception e) {
                    plugin.getLogger().warning(String.format(
                            "Load module '%s' failed: %s", module, e.getMessage()));
                    continue;
                }
            }
            plugin.getLogger().warning(String.format(
                    "Loaded module '%s'(%s) %s.",
                    module, realModule,
                    nativeModule.isInitialized() ? "successfully" : "failed"));
        }
    }

    public static String[] getDynamicFileType() {
        switch (OS) {
            case OS_WINDOWS:
                return DYNAMIC_FILE_TYPE_WINDOWS;
            case OS_MACOS:
                return DYNAMIC_FILE_TYPE_MACOS;
            case OS_LINUX:
                return DYNAMIC_FILE_TYPE_LINUX;
            default:
                return new String[0];
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

    private static NativeLibraryConfig getNativeConfig(Plugin plugin,
                                                       String baseUrl,
                                                       File nativeFolder) throws IOException {
        File nativeConfigFile = new File(nativeFolder, "natives.json");
        if (!nativeConfigFile.exists()) {
            downloadLibraryConfig(plugin, baseUrl, nativeConfigFile);
        }
        return new Gson()
                .fromJson(new FileReader(nativeConfigFile), NativeLibraryConfig.class);
    }

    private static void downloadLibraryConfig(Plugin plugin,
                                              String baseUrl,
                                              File nativeConfigFile) {
        baseUrl += "/natives/natives.json";
        plugin.getLogger()
                .info(String.format(
                        "Not found natives.json, download from %s",
                        baseUrl
                ));
        try {
            byte[] bytes = downloadFile(baseUrl);
            if (nativeConfigFile.getParent() != null && !nativeConfigFile.getParentFile().exists()) {
                nativeConfigFile.getParentFile().mkdirs();
            }
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

    private static void checkFile(Plugin plugin,
                                  File lib,
                                  Config config,
                                  NativeLibraryConfig libraryConfig,
                                  String module) {
        if (!lib.exists() || config.isAlwaysCheckHashes()) {
            List<String> urls = new ArrayList<>();
            urls.add(config.getProjectUrl() + "/natives");
            urls.addAll(Arrays.asList(libraryConfig.getUrls()));
            for (String baseUrl : urls) {
                baseUrl = String.join("/", baseUrl, OS, ARCH, module);
                String fileUrl = baseUrl + "/" + lib.getName();
                String hashUrl = fileUrl + ".hash";
                try {
                    int retry = 3;
                    while (retry > 0) {
                        if (!lib.exists()) {
                            plugin.getLogger()
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
                        plugin
                                .getLogger()
                                .info("Downloading library hash file from " + hashUrl);
                        String targetHash = new String(downloadFile(hashUrl));
                        String downloadedHash = sha256(lib);
                        if (Objects.equals(downloadedHash, targetHash)) {
                            break;
                        }
                        plugin
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
                        plugin.getLogger().warning(
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
