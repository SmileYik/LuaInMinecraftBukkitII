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
public interface ILuacage extends ILuacageRepository {

    /**
     * clean index cache and update index.
     */
    void cleanCache();

    /**
     * clean index cache and update index.
     */
    void update();

    /**
     * install package
     * @param meta  the package
     * @param force is re-download from repository.
     */
    void installPackage(@NotNull LuacageJsonMeta meta, boolean force);

    /**
     * install package
     * @param meta  the package
     * @param force is re-download from repository.
     * @param onConflict on conflict
     */
    void installPackage(
            @NotNull LuacageJsonMeta meta,
            boolean force,
            @NotNull Function<List<LuacageJsonMeta>, LuacageJsonMeta> onConflict
    );

    /**
     * find packages the target package depends.
     * @param meta the package
     * @return return the dependent list. this list just include dependents.
     */
    List<LuacageJsonMeta> findDepends(@NotNull LuacageJsonMeta meta);

    /**
     * find packages the target package depends.
     * @param meta the package
     * @param onConflict on conflict
     * @return return the dependent list. this list just include dependents.
     */
    List<LuacageJsonMeta> findDepends(
            @NotNull LuacageJsonMeta meta,
            @NotNull Function<List<LuacageJsonMeta>, LuacageJsonMeta> onConflict
    );

    /**
     * get the installed package list
     * @return list of installed packages.
     */
    List<LuacageJsonMeta> installedPackages();

    /**
     * uninstall package
     * @param meta package information
     */
    boolean uninstallPackage(@NotNull LuacageJsonMeta meta);

    /**
     * remove the packages not installed by manual and it is not needed by other packages
     */
    List<LuacageJsonMeta> removeUselessPackages();

    /**
     * get package install dir.
     * @param meta package information
     * @return the package installed dir
     */
    File getInstallDir(@NotNull LuacageCommonMeta meta);

    /**
     * load packages to lua env
     */
    void loadPackages();

    boolean loadPackage(LuacageJsonMeta pkg);
}
