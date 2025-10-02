package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import com.google.gson.Gson;
import org.eu.smileyik.luaInMinecraftBukkitII.config.LuacageConfig;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
import org.eu.smileyik.luaInMinecraftBukkitII.reflect.LuaTable2Object;
import org.eu.smileyik.luaInMinecraftBukkitII.util.HashUtil;
import org.eu.smileyik.luaInMinecraftBukkitII.util.HexUtil;
import org.eu.smileyik.luaInMinecraftBukkitII.util.StaticResourceDownloader;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Luacage implements ILuacageRepository, ILuacage {
    public static final String PACKAGE_META_NAME = "luacage.json";
    public static final String PACKAGE_DIR_NAME = "packages";
    public static final String PACKAGE_LUA_NAME = "package.lua";

    private static final ReentrantLock REPO_LOCK = new ReentrantLock();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<ILuacageRepository> repositoryCleanFuture = null;
    private static ILuacageRepository repository;

    private final ILuaStateEnvInner env;
    private final LuacageConfig config;
    private final File baseDir;
    private final Logger logger;
    private final Map<String, LuacageLuaMeta> loadedPackages = new HashMap<>();

    private final Function<List<LuacageJsonMeta>, LuacageJsonMeta> DEFAULT_ON_CONFLICT;

    /**
     *
     * @param env     lua 环境
     * @param config  配置
     * @param baseDir 仓库数据存储位置
     * @param logger  日志
     */
    public Luacage(ILuaStateEnvInner env, LuacageConfig config, File baseDir, Logger logger) {
        this.env = env;
        this.config = config;
        this.baseDir = baseDir;
        this.logger = logger;

        DEFAULT_ON_CONFLICT = packages -> {
            Map<String, LuacageJsonMeta> packageMap = new HashMap<>();
            for (LuacageJsonMeta dependency : packages) {
                packageMap.put(dependency.getSource(), dependency);
            }
            for (LuacageConfig.Source source : config.getSources()) {
                if (packageMap.containsKey(source.getName())) {
                    return packageMap.get(source.getName());
                }
            }
            return null;
        };
    }

    protected ILuacageRepository getRepository(String name, String baseUrl, boolean noCache) {
        REPO_LOCK.lock();
        try {
            File file = new File(baseDir, getRepositoryCacheFileName(name));
            if (noCache && file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                String url = baseUrl + PACKAGE_META_NAME;
                String hashUrl = baseUrl + PACKAGE_META_NAME + ".hash";
                try (
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                ) {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    StaticResourceDownloader.download(url, 8192, 60, (bytes, len) -> {
                        digest.update(bytes, 0, len);
                        bos.write(bytes, 0, len);
                    });
                    bos.flush();
                    String hashes = HexUtil.bytesToHex(digest.digest());
                    String remoteHashes = StaticResourceDownloader.getUrlContents(hashUrl);
                    if (!HashUtil.isEqualsHashString(hashes, remoteHashes)) {
                        logger.warning("Luacage hash verification failed for repository " + name + ": " + baseUrl);
                        file.delete();
                        return null;
                    }
                } catch (NoSuchAlgorithmException | IOException | ExecutionException | InterruptedException |
                         TimeoutException e) {
                    logger.warning("Luacage index failed download for repository " + name + ": " + baseUrl + ": " + e);
                    if (file.exists()) file.delete();
                    DebugLogger.debug(e);
                }
            }
            if (file.exists()) {
                try {
                    return new LuacageRepository(name, file);
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
            return null;
        } finally {
            REPO_LOCK.unlock();
        }
    }

    protected synchronized ILuacageRepository getRepository(boolean noCache) {
        if (noCache) {
            if (repositoryCleanFuture != null) {
                repositoryCleanFuture.cancel(false);
            }
            repository = null;
        }

        if (repository == null) {
            LuacageConfig.Source[] sources = config.getSources();
            LuacageRepositoryBundle bundle = new LuacageRepositoryBundle(sources.length);
            for (LuacageConfig.Source source : sources) {
                ILuacageRepository repo = getRepository(source.getName(), source.getUrl(), noCache);
                if (repo != null) {
                    bundle.add(repo);
                }
            }
            repository = bundle;
        }
        if (repositoryCleanFuture != null) {
            repositoryCleanFuture.cancel(false);
        }
        repositoryCleanFuture = scheduler.schedule(() -> repository = null, 5, TimeUnit.MINUTES);
        return repository;
    }

    protected ILuacageRepository getRepository() {
        return getRepository(false);
    }

    protected String getRepositoryCacheFileName(String name) {
        return String.format("%s-luacages.json", name);
    }

    protected String getBaseUrl(@NotNull LuacageJsonMeta meta) {
        for (LuacageConfig.Source source : config.getSources()) {
            if (source.getName().equals(meta.getSource())) {
                return source.getUrl();
            }
        }
        return null;
    }

    protected boolean doInstallPackage(@NotNull LuacageJsonMeta meta, boolean force) {
        File installDir = getInstallDir(meta);
        REPO_LOCK.lock();
        try {
            if (force) {
                deleteFolder(installDir);
            } else if (installDir.exists()) {
                return true;
            }
            String[] files = meta.getFiles();
            String[] hashes = meta.getHashes();
            String baseUrl = getBaseUrl(meta);
            for (int i = 0; i < files.length; i++) {
                String path = files[i];
                String hash = hashes[i];
                String url = baseUrl + PACKAGE_DIR_NAME + "/" + meta.getName() + path;
                File file = new File(installDir, path);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                logger.info("Downloading package file '" + path + "' from " + url);
                String downloadHash = StaticResourceDownloader.download(url, 8192, 60, file);
                if (!HashUtil.isEqualsHashString(hash, downloadHash)) {
                    deleteFolder(installDir);
                    logger.warning(String.format("File '%s' hash mismatch, expected '%s', got '%s'. " +
                            "Attempt to update the database source index.", path, hash, downloadHash));
                    return false;
                }
            }
        } catch (Exception e) {
            deleteFolder(installDir);
            logger.warning("Luacage install failed, attempt to update the database source index: " + e.getMessage());
            DebugLogger.debug(e);
            return false;
        } finally {
            REPO_LOCK.unlock();
        }
        return true;
    }

    protected Result<List<LuacageJsonMeta>, Collection<LuacageJsonMeta>> sort(List<LuacageJsonMeta> list) {
        Map<String, LuacageJsonMeta> map = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (LuacageJsonMeta meta : list) {
            map.put(meta.getName(), meta);
            inDegree.putIfAbsent(meta.getName(), 0);
            for (String depend : meta.getDependPackages()) {
                inDegree.put(depend, inDegree.getOrDefault(depend, 0) + 1);
            }
        }

        List<LuacageJsonMeta> sorted = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        inDegree.forEach((k, v) -> {
            if (v == 0) queue.add(k);
        });
        while (!queue.isEmpty()) {
            String name = queue.poll();
            LuacageJsonMeta meta = map.get(name);
            sorted.add(meta);
            for (String depend : meta.getDependPackages()) {
                int in = inDegree.getOrDefault(depend, 0) - 1;
                inDegree.put(depend, in);
                if (in == 0) queue.add(depend);
            }
        }
        if (sorted.size() != map.size()) {
            Set<LuacageJsonMeta> circle = new HashSet<>(map.values());
            sorted.forEach(circle::remove);
            return Result.failure(circle);
        }

        return Result.success(sorted);
    }

    protected Result<Void, Collection<LuacageJsonMeta>> updateInstalledPackagesJson(List<LuacageJsonMeta> list) throws IOException {
        Result<List<LuacageJsonMeta>, Collection<LuacageJsonMeta>> result = sort(list);
        if (result.isError()) return result.justCast();

        List<LuacageJsonMeta> sorted = result.getValue();
        String json = new Gson().toJson(sorted);
        File file = new File(env.getRootDir(), String.format("%s.installed.luacage.json", env.getId()));
        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
        return Result.success();
    }

    @Override
    public List<LuacageJsonMeta> getPackages() {
        return getRepository().getPackages();
    }

    @Override
    public @NotNull List<LuacageJsonMeta> findPackages(String packageName, String desc, short searchType) {
        return getRepository().findPackages(packageName, desc, searchType);
    }

    @Override
    public void cleanCache() {
        getRepository(true);
    }

    @Override
    public void update() {
        getRepository(true);
    }

    @Override
    public synchronized void installPackage(@NotNull LuacageJsonMeta meta, boolean force) {
        installPackage(meta, force, DEFAULT_ON_CONFLICT);
    }

    @Override
    public synchronized void installPackage(
            @NotNull LuacageJsonMeta meta,
            boolean force,
            @NotNull Function<List<LuacageJsonMeta>, LuacageJsonMeta> onConflict
    ) {
        List<LuacageJsonMeta> installedPackages = installedPackages();
        Set<String> installedNames = installedPackages.parallelStream()
                .map(LuacageJsonMeta::getName)
                .collect(Collectors.toSet());
        List<LuacageJsonMeta> waitInstallPackages = findDepends(meta, onConflict);
        waitInstallPackages.add(meta);
        meta.setManual(true);
        Result<List<LuacageJsonMeta>, Collection<LuacageJsonMeta>> sortResult = sort(waitInstallPackages);
        if (sortResult.isError()) {
            throw new RuntimeException(String.format("Package '%s' include circular dependency: %s", meta.getName(), sortResult.getValue()));
        }
        List<LuacageJsonMeta> pkgs = sortResult.getValue();
        List<LuacageJsonMeta> installed = new ArrayList<>();
        boolean failed = false;
        for (LuacageJsonMeta pkgMeta : pkgs) {
            if (installedNames.contains(pkgMeta.getName())) continue;
            installed.add(pkgMeta);
            // just force install the package we pointed.
            if (!doInstallPackage(pkgMeta, force && pkgMeta.getName().equals(meta.getName()))) {
                failed = true;
                break;
            }
        }

        // update json
        if (!failed) {
            installed.addAll(pkgs);
            try {
                Result<Void, Collection<LuacageJsonMeta>> updateResult = updateInstalledPackagesJson(installed);
                if (updateResult.isError()) {
                    throw new RuntimeException(String.format("Package '%s' include circular dependency: %s", meta.getName(), sortResult.getValue()));
                }
            } catch (IOException e) {
                failed = true;
            }
        }

        if (failed) {
//            for (LuacageJsonMeta pkgMeta : installed) {
//                deleteFolder(getInstallDir(pkgMeta));
//            }
            throw new RuntimeException(String.format("Package '%s' install failure.", meta.getName()));
        }
    }

    @Override
    public List<LuacageJsonMeta> findDepends(@NotNull LuacageJsonMeta meta) {
        return findDepends(meta, DEFAULT_ON_CONFLICT);
    }

    @Override
    public List<LuacageJsonMeta> findDepends(
            @NotNull LuacageJsonMeta meta,
            @NotNull Function<List<LuacageJsonMeta>, LuacageJsonMeta> onConflict
    ) {
        List<LuacageJsonMeta> depends = new ArrayList<>();
        List<String> notfound = new ArrayList<>();

        Queue<LuacageJsonMeta> queue = new LinkedList<>();
        queue.add(meta);
        while (!queue.isEmpty()) {
            LuacageJsonMeta dependency = queue.poll();
            String[] dependPackages = dependency.getDependPackages();
            ILuacageRepository repo = getRepository();
            if (dependPackages != null) {
                for (String dependPackage : dependPackages) {
                    List<LuacageJsonMeta> packages = repo.findPackages(dependPackage, null, SEARCH_TYPE_PKG_NAME_EXACTLY);
                    LuacageJsonMeta pickup;
                    if (packages.isEmpty()) {
                        pickup = null;
                    } else if (packages.size() == 1) {
                        pickup = packages.get(0);
                    } else {
                        pickup = onConflict.apply(packages);
                    }
                    if (pickup == null) {
                        notfound.add(dependPackage);
                    } else {
                        depends.add(pickup);
                        queue.add(pickup);
                    }
                }
            }
        }
        if (!notfound.isEmpty()) {
            throw new RuntimeException("Dependencies not found: " + notfound);
        }
        return depends;
    }

    @Override
    public synchronized List<LuacageJsonMeta> installedPackages() {
        File file = new File(env.getRootDir(), String.format("%s.installed.luacage.json", env.getId()));
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try {
            return new ArrayList<>(new LuacageRepository(file).getPackages());
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public synchronized void uninstallPackage(@NotNull LuacageJsonMeta meta) {
        List<LuacageJsonMeta> installedPackages = installedPackages();
        for (LuacageJsonMeta installedPackage : installedPackages) {
            if (installedPackage.getName().equals(meta.getName())) {
                installedPackages.remove(installedPackage);
                break;
            }
        }
        try {
            updateInstalledPackagesJson(installedPackages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized List<LuacageJsonMeta> removeUselessPackages() {
        List<LuacageJsonMeta> installedPackages = installedPackages();
        Map<String, LuacageJsonMeta> map = new HashMap<>();
        Set<String> deleted = new HashSet<>();

        // init installed packages information
        for (LuacageJsonMeta installedPackage : installedPackages) {
            map.put(installedPackage.getName(), installedPackage);
            if (!installedPackage.isManual()) {
                deleted.add(installedPackage.getName());
            }
        }

        // find need packages
        for (LuacageJsonMeta installedPackage : installedPackages) {
            if (!installedPackage.isManual()) continue;
            Set<String> checked = new HashSet<>();
            Queue<LuacageJsonMeta> queue = new LinkedList<>();
            queue.add(installedPackage);
            checked.add(installedPackage.getName());
            while (!queue.isEmpty()) {
                LuacageJsonMeta dependency = queue.poll();
                if (checked.add(dependency.getName())) {
                    deleted.remove(dependency.getName());
                }
                String[] dependPackages = dependency.getDependPackages();
                for (String dependPackage : dependPackages) {
                    LuacageJsonMeta meta = map.get(dependPackage);
                    if (meta != null) queue.add(meta);
                }
            }
        }

        // remove
        List<LuacageJsonMeta> removed = installedPackages.parallelStream()
                .filter(it -> deleted.contains(it.getName()))
                .collect(Collectors.toList());
        installedPackages = installedPackages.parallelStream()
                .filter(it -> !deleted.contains(it.getName()))
                .collect(Collectors.toList());
        sort(installedPackages)
                .ifSuccessThen(newList -> {
                    try {
                        updateInstalledPackagesJson(newList);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .ifFailureThen(circularDependencies -> {
                    logger.severe("[LuaEnv " + env.getId() + "] Circular dependencies found: " + circularDependencies);
                });
        return removed;
    }

    @Override
    public File getInstallDir(@NotNull LuacageCommonMeta meta) {
        File packageFile = new File(baseDir, PACKAGE_DIR_NAME + File.separator + meta.getName());
        if (!packageFile.exists()) {
            packageFile.mkdirs();
        }
        return packageFile;
    }

    @Override
    public void loadPackages() {
        loadedPackages.clear();
        List<LuacageJsonMeta> installedPackages = installedPackages();
        List<LuacageLuaMeta> packages = new ArrayList<>(installedPackages.size());
        Set<String> failed = new HashSet<>();
        LuaStateFacade lua = env.getLuaState();
        lua.lock(l -> {
            for (LuacageJsonMeta installedPackage : installedPackages) {
                LuacageLuaMeta meta = loadPackageLua(lua, installedPackage, failed);
                if (meta != null) {
                    packages.add(meta);
                }
            }

            for (LuacageLuaMeta meta : packages) {
                loadPackage(lua, meta);
            }
        });
    }

    @Override
    public boolean loadPackage(LuacageJsonMeta pkg) {
        List<LuacageJsonMeta> pkgs = findDepends(pkg);
        pkgs.add(pkg);
        return sort(pkgs)
                .ifSuccessThen(packs -> {
                    LuaStateFacade lua = env.getLuaState();
                    lua.lock(l -> {
                        List<LuacageLuaMeta> packages = new ArrayList<>();
                        for (LuacageJsonMeta p : packs) {
                            if (loadedPackages.containsKey(p.getName())) continue;
                            LuacageLuaMeta meta = loadPackageLua(lua, p, new HashSet<>());
                            if (meta != null) {
                                packages.add(meta);
                            }
                        }

                        for (LuacageLuaMeta meta : packages) {
                            loadPackage(lua, meta);
                        }
                    });
                })
                .isSuccess();
    }

    protected boolean loadPackage(LuaStateFacade lua, LuacageLuaMeta pkg) {
        String main = pkg.getMain();
        if (main != null) {
            if (main.startsWith("/")) {
                main = main.substring(1);
            }
            if (main.toLowerCase(Locale.ENGLISH).endsWith(".lua")) {
                main = main.substring(0, main.length() - 4);
            }
            return lua.evalString(String.format("require('@%s/%s')", pkg.getName(), main))
                    .ifFailureThen(err -> {
                        logger.warning(String.format("Failed load main file of package '%s': %s", pkg.getName(), err.getMessage()));
                        DebugLogger.debug(err);
                    })
                    .isSuccess();
        }
        return true;
    }

    protected LuacageLuaMeta loadPackageLua(LuaStateFacade lua, LuacageJsonMeta installedPackage, Set<String> failed) {
        if (installedPackage.getDependPackages() != null) {
            boolean skip = false;
            for (String dependPackage : installedPackage.getDependPackages()) {
                if (failed.contains(dependPackage)) {
                    logger.warning(String.format("Failed load package '%s': depends '%s' is not loaded", installedPackage.getName(), dependPackage));
                    failed.add(installedPackage.getName());
                    skip = true;
                    break;
                }
            }
            if (skip) return null;
        }

        File installDir = getInstallDir(installedPackage);
        File file = new File(installDir, PACKAGE_LUA_NAME);
        Result<Integer, LuaException> metaResult = env.evalFile(file.getAbsolutePath());
        if (metaResult.isError()) {
            failed.add(installedPackage.getName());
            LuaException error = metaResult.getError();
            logger.warning(String.format("Failed load 'package.lua' file of package '%s': %s", installedPackage.getName(), error.getMessage()));
            DebugLogger.debug(error);
            return null;
        }
        Result<LuacageLuaMeta, Exception> covertResult = lua.toJavaObject(-1)
                .mapResultValue(obj -> {
                    if (obj instanceof LuaTable) {
                        return LuaTable2Object.covert((LuaTable) obj, LuacageLuaMeta.class);
                    }
                    return null;
                });
        if (covertResult.isError() || covertResult.isSuccess() && covertResult.getValue() == null) {
            failed.add(installedPackage.getName());
            Exception error = covertResult.getError();
            logger.warning(String.format("Failed load 'package.lua' file of package '%s': %s", installedPackage.getName(), error.getMessage()));
            DebugLogger.debug(error);

            return null;
        }
        LuacageLuaMeta meta = covertResult.getValue();
        if (meta.isRunnable()) {
            return meta;
        } else {
            failed.add(installedPackage.getName());
            logger.warning(String.format("Failed load package '%s': it's not support your server: %s", installedPackage.getName(), meta.getReason()));
        }
        return null;
    }

    protected void deleteFolder(File folder) {
        if (!folder.exists()) return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                }
                file.delete();
            }
        }
        folder.delete();
    }
}
