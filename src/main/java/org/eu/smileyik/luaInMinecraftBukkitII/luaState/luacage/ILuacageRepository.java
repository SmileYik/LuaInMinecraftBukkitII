package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import java.util.Collection;
import java.util.List;

public interface ILuacageRepository {
    public static final byte SEARCH_TYPE_PKG_NAME_ONLY = 0;
    public static final byte SEARCH_TYPE_DESC_NAME_ONLY = 1;
    public static final byte SEARCH_TYPE_PKG_NAME_AND_DESC = 2;

    Collection<LuacageJsonMeta> getPackages();

    List<LuacageJsonMeta> findPackages(String packageName, String desc, short searchType);

    List<LuacageJsonMeta> findPackages(String keyword);

    List<LuacageJsonMeta> findPackagesByName(String packageName);
}
