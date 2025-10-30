package org.eu.smileyik.luaInMinecraftBukkitII.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageCommonMeta;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageJsonMeta;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageLuaMeta;
import org.eu.smileyik.luaInMinecraftBukkitII.reflect.LuaTable2Object;
import org.eu.smileyik.luaInMinecraftBukkitII.util.HashUtil;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.type.LuaTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.Luacage.*;

public class LuacageDatabaseGenerator {

    public static final String BUILD_PATH = System.getenv("luainminecraftbukkit_luacage_build");
    private static final String NAME_PATTERN = "[a-zA-Z][a-zA-Z0-9_-]+";

    public static void run() {
        if (BUILD_PATH == null) return;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Object> stop = executorService.submit(() -> {
            while (true) {
                LuaStateFacade facade = null;
                try {
                    facade = getLuaStateFacade();
                } catch (Exception ignore) {

                }
                if (facade != null) break;
                Thread.sleep(1000);
            }
            try {
                LuacageDatabaseGenerator.run(BUILD_PATH, BUILD_PATH + "/" + PACKAGE_META_NAME);
            } catch (Exception ignore) {

            }
            return null;
        });
        try {
            stop.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
            LuaInMinecraftBukkit.instance().getScheduler().runTask(LuaInMinecraftBukkit.instance(), () -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
            });
        }
    }

    public static void run(String repoPath, String outPath) throws Exception {
        try {
            doRun(repoPath, outPath);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
            throw e;
        }
    }

    protected static void doRun(String repoPath, String outPath) throws Exception {
        File repo = new File(repoPath);
        File pkgDir = new File(repoPath, PACKAGE_DIR_NAME);
        if (!pkgDir.exists()) {
            pkgDir.mkdirs();
        }
        List<LuacageJsonMeta> database = getDatabase(repo);
        File[] files = pkgDir.listFiles();
        if (files == null) {
            return;
        }
        Set<String> ids = new HashSet<>();
        for (File file : files) {
            if (file.isDirectory()) {
                ids.add(file.getName());
            }
        }

        List<String> remove = new ArrayList<>();
        List<String> update = new ArrayList<>();
        for (LuacageJsonMeta meta : database) {
            if (!ids.remove(meta.getName())) {
                remove.add(meta.getName());
            } else {
                LuacageLuaMeta luacageLuaMeta = loadLuaMeta(getLuaStateFacade(), new File(pkgDir, meta.getName() + "/" + PACKAGE_LUA_NAME));
                if (!Objects.equals(luacageLuaMeta.getVersion(), meta.getVersion())) {
                    update.add(meta.getName());
                }
            }
        }
        for (String id : update) {
            updateMetaList(repoPath, new File(repo, PACKAGE_META_NAME).getAbsolutePath(), id);
        }
        for (String id : ids) {
            updateMetaList(repoPath, new File(repo, PACKAGE_META_NAME).getAbsolutePath(), id);
        }
        database = getDatabase(repo);
        database = database.parallelStream().filter(it -> !remove.contains(it.getName())).collect(Collectors.toList());;
        writeJson(database, outPath);
    }

    public static void updateMetaList(String repoPath, String outPath, String name) throws Exception {
        if (!name.matches(NAME_PATTERN)) {
            throw new Exception("Invalid name " + name + ", name not matches pattern " + NAME_PATTERN);
        }
        File repo = new File(repoPath);
        File pkgDir = new File(repoPath, PACKAGE_DIR_NAME);
        List<LuacageJsonMeta> database = getDatabase(repo);

        File packageDir = new File(pkgDir, name);
        if (!packageDir.exists()) {
            throw new FileNotFoundException(packageDir.getAbsolutePath());
        }
        File pkgMetaFile = new File(packageDir, PACKAGE_LUA_NAME);
        LuacageLuaMeta meta = loadLuaMeta(getLuaStateFacade(), pkgMetaFile);
        LuacageJsonMeta jsonMeta = null;
        int idx = 0;
        for (LuacageJsonMeta find : database) {
            if (find.getName().equals(meta.getName())) {
                jsonMeta = find;
                break;
            }
            idx += 1;
        }
        if (jsonMeta == null) {
            jsonMeta = new LuacageJsonMeta();
            jsonMeta.setCreatedAt(System.currentTimeMillis());
            idx = -1;
        }

        jsonMeta.setName(name);
        jsonMeta.setVersion(meta.getVersion());
        jsonMeta.setAuthors(meta.getAuthors());
        jsonMeta.setDescription(meta.getDescription());
        jsonMeta.setLuaVersion(meta.getLuaVersion());
        jsonMeta.setDependPackages(meta.getDependPackages());
        jsonMeta.setDependPlugins(meta.getDependPlugins());

        List<String> files = new ArrayList<>();
        List<String> hashes = new ArrayList<>();
        Files.walkFileTree(packageDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String fileName = dir.getFileName().toString();
                if (fileName.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isRegularFile()) {
                    return FileVisitResult.CONTINUE;
                }
                String p = file.toFile().getAbsolutePath();
                int i = p.indexOf(name);
                if (i != -1) {
                    String filePath = p.substring(i + name.length()).replace("\\", "/");
                    if (filePath.startsWith("/.git")) return FileVisitResult.CONTINUE;
                    files.add(filePath);
                    try {
                        String hash = HashUtil.sha256(file.toFile());
                        hashes.add(hash);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });

        String[] fs = new String[files.size()];
        String[] hs = new String[hashes.size()];
        for (int i = 0; i < fs.length; i++) {
            fs[i] = files.get(i).replace("\\", "/");
            hs[i] = hashes.get(i);
        }
        jsonMeta.setFiles(fs);
        jsonMeta.setHashes(hs);
        jsonMeta.setUpdateAt(System.currentTimeMillis());
        if (idx == -1) {
            database.add(jsonMeta);
        } else {
            database.set(idx, jsonMeta);
        }
        writeJson(database, outPath);
    }

    private static LuaStateFacade getLuaStateFacade() {
        ILuaStateManager luaStateManager = LuaInMinecraftBukkit.instance().getLuaStateManager();
        if (luaStateManager == null) return null;
        Collection<ILuaStateEnv> scriptEnvs = luaStateManager.getScriptEnvs();
        ILuaStateEnvInner env = (ILuaStateEnvInner) scriptEnvs.stream().findFirst().orElse(null);
        if (env == null) {
            throw new IllegalStateException("LuaInMinecraftBukkit has no lua env");
        }
        return env.getLuaState();
    }

    private static List<LuacageJsonMeta> getDatabase(File repo) throws FileNotFoundException {
        File file = new File(repo, PACKAGE_META_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        List<LuacageJsonMeta> list = new Gson().fromJson(new FileReader(file), new TypeToken<List<LuacageJsonMeta>>() {}.getType());
        return new ArrayList<>(list);
    }

    private static void writeJson(List<LuacageJsonMeta> list, String outPath) throws IOException, NoSuchAlgorithmException {
        list.sort(Comparator.comparing(LuacageCommonMeta::getName));
        String json = new Gson().toJson(list);
        Path path = Paths.get(outPath);
        Files.write(path, json.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE
        );
        String hash = HashUtil.sha256(path.toFile());
        Files.write(Paths.get(outPath + ".hash"), hash.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE
        );
    }

    private static LuacageLuaMeta loadLuaMeta(LuaStateFacade lua, File luaFile) throws Exception {
        lua.lock();
        try {
            lua.evalFile(luaFile.getAbsolutePath()).justThrow();
            LuaTable table = (LuaTable) lua.toJavaObject(-1).getOrThrow();
            return LuaTable2Object.covert(table, LuacageLuaMeta.class).getOrThrow();
        } finally {
            lua.unlock();
        }
    }
}
