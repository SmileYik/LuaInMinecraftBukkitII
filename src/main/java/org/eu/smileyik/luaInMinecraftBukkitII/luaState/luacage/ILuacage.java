package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.function.Function;

/**
 * luacage is a lua scripts package manager.
 * luacage based on lua environment, every lua environment need a luacage instance.
 * And luacage should not break currently script loader. it should be an additional loader.
 */
public interface ILuacage {

    void cleanCache();

    void installPackage(@NotNull LuacageJsonMeta meta, boolean force);

    void installPackage(
            @NotNull LuacageJsonMeta meta,
            boolean force,
            @NotNull Function<List<LuacageJsonMeta>, LuacageJsonMeta> onConflict
    );

    List<LuacageJsonMeta> findDepends(@NotNull LuacageJsonMeta meta);

    List<LuacageJsonMeta> findDepends(
            @NotNull LuacageJsonMeta meta,
            @NotNull Function<List<LuacageJsonMeta>, LuacageJsonMeta> onConflict
    );

    List<LuacageJsonMeta> installedPackages();

    void uninstallPackage(@NotNull LuacageJsonMeta meta);

    File getInstallDir(@NotNull LuacageCommonMeta meta);

    void loadPackages();

    boolean loadPackage(LuacageJsonMeta pkg);
}
