package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface ILuacageRepository {
    public static final byte SEARCH_TYPE_PKG_NAME_ONLY = 0;
    public static final byte SEARCH_TYPE_DESC_NAME_ONLY = 1;
    public static final byte SEARCH_TYPE_PKG_NAME_AND_DESC = 2;
    public static final byte SEARCH_TYPE_PKG_NAME_EXACTLY = 3;

    public static final ILuacageRepository EMPTY = new ILuacageRepository() {
        @Override
        public Collection<LuacageJsonMeta> getPackages() {
            return Collections.emptyList();
        }

        @Override
        public @NotNull List<LuacageJsonMeta> findPackages(String packageName, String desc, short searchType) {
            return Collections.emptyList();
        }
    };

    Collection<LuacageJsonMeta> getPackages();

    @NotNull
    List<LuacageJsonMeta> findPackages(String packageName, String desc, short searchType);

    default List<LuacageJsonMeta> findPackages(String keyword) {
        return findPackages(keyword, keyword, SEARCH_TYPE_PKG_NAME_AND_DESC);
    }

    default List<LuacageJsonMeta> findPackagesByName(String packageName) {
        return findPackages(packageName, null, SEARCH_TYPE_PKG_NAME_ONLY);
    }
}
