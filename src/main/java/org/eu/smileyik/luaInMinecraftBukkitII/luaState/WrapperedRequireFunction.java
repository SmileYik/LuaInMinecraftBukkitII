package org.eu.smileyik.luaInMinecraftBukkitII.luaState;

import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.LuaIOHelper;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.ILuacage;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageJsonMeta;
import org.eu.smileyik.luajava.JavaFunction;
import org.eu.smileyik.luajava.LuaException;
import org.eu.smileyik.luajava.LuaState;
import org.eu.smileyik.luajava.LuaStateFacade;
import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class WrapperedRequireFunction extends JavaFunction {
    private static int COUNTER = 0;
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private final ILuaCallable originRequire;
    private final File tempDir, rootDir;
    private final ILuaStateEnv env;

    /**
     * Constructor that receives a LuaState.
     *
     * @param L LuaState object associated with this JavaFunction object
     */
    public WrapperedRequireFunction(LuaStateFacade L, ILuaCallable originRequire, File rootDir, ILuaStateEnv env) {
        super(L);
        this.originRequire = originRequire;
        this.rootDir = rootDir;
        this.tempDir = new File(rootDir, "temp");
        this.env = env;
        if (!this.tempDir.exists()) {
            this.tempDir.mkdirs();
        }
    }

    @Override
    public int execute() throws LuaException {
        LuaState l = L.getLuaState();
        int top = l.getTop();
        if (top <= 1) {
            throw new LuaException("Attempted to call require() on an empty state");
        }
        if (!l.isString(-1)) {
            throw new LuaException("Attempted to call require() on an invalid state, the first param type should be a string");
        }
        String module = l.toString(-1);
        // require network
        if (module.startsWith("http")) {
            DebugLogger.debug("Requesting module: " + module);
            File downloaded = null;
            try {
                downloaded = download(module);
                String str1 = rootDir.getAbsolutePath();
                String str2 = downloaded.getAbsolutePath();
                module = str2.substring(str1.length() + 1);
                Result<Void, ? extends LuaException> result = originRequire.call(module)
                        .mapResultValue(L::rawPushObjectValue);
                if (result.isError()) {
                    originRequire.call(module.replaceAll("[/\\\\]", "."))
                            .mapResultValue(L::rawPushObjectValue)
                            .justThrow(LuaException.class);
                }
            } catch (IOException e) {
                DebugLogger.debug(e);
                throw new LuaException("Failed to download file: " + module + ": " + e.getMessage(), e);
            } finally {
                DebugLogger.debug("Finished requesting module: " + module);
            }
        } else {
            // require package
            if (module.startsWith("@")) {
                final String finalModule = module.substring(1);
                ILuacage luacage = env.getLuacage();
                LuacageJsonMeta pkg = luacage.installedPackages()
                        .parallelStream()
                        .filter(it -> finalModule.startsWith(it.getName() + ".") || finalModule.startsWith(it.getName() + "/"))
                        .findAny()
                        .orElse(null);
                if (pkg != null) {
                    File parentFile = luacage.getInstallDir(pkg).getParentFile();
                    module = parentFile.getAbsolutePath() + "/" + module.substring(1);
                    module = module.substring(new File(".").getAbsolutePath().length() - 1);
                }
            }
            originRequire.call(module)
                    .mapResultValue(L::rawPushObjectValue)
                    .justThrow(LuaException.class);
        }

        return 1;
    }

    private synchronized File download(String urlStr) throws IOException {
        String fileName = urlStr.replaceAll("[/\\\\]", "/");
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        String fileNameWithSuffix = fileName;
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }

        File out = new File(tempDir, fileNameWithSuffix);
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();
        try (
                BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(out.toPath()));
        ) {
            LuaIOHelper.transfer(bis, bos, 2048);
        }
        return new File(tempDir, fileName);
    }
}
