package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class LuacageRepository implements ILuacageRepository {
    private final List<LuacageJsonMeta> list;

    public LuacageRepository(File file) throws FileNotFoundException {
        list = new Gson().fromJson(new FileReader(file), new TypeToken<List<LuacageJsonMeta>>() {}.getType());
    }

    public LuacageRepository(String repoName, File file) throws FileNotFoundException {
        list = new Gson().fromJson(new FileReader(file), new TypeToken<List<LuacageJsonMeta>>() {}.getType());
        list.parallelStream().forEach(l -> l.setSource(repoName));
    }

    @Override
    public Collection<LuacageJsonMeta> getPackages() {
        return Collections.unmodifiableList(list);
    }

    @Override
    @NotNull
    public List<LuacageJsonMeta> findPackages(String packageName, String desc, short searchType) {
        return list.parallelStream()
                .filter(it -> {
                    if (searchType == SEARCH_TYPE_PKG_NAME_EXACTLY && Objects.equals(packageName, it.getName())) {
                        return true;
                    } else if (searchType == SEARCH_TYPE_PKG_NAME_ONLY
                            && hasString(packageName)
                            && it.getName().toLowerCase(Locale.ENGLISH).contains(packageName.toLowerCase(Locale.ENGLISH))) {
                        return true;
                    } else if (searchType == SEARCH_TYPE_DESC_NAME_ONLY
                            && hasString(desc)
                            && it.getDescription().toLowerCase(Locale.ENGLISH).contains(desc.toLowerCase(Locale.ENGLISH))) {
                        return true;
                    } else if (searchType == SEARCH_TYPE_PKG_NAME_AND_DESC) {
                        if (hasString(packageName) && it.getName().toLowerCase(Locale.ENGLISH).contains(packageName.toLowerCase(Locale.ENGLISH))) {
                            return true;
                        } else return hasString(desc) && it.getDescription().toLowerCase(Locale.ENGLISH).contains(desc.toLowerCase(Locale.ENGLISH));
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private boolean hasString(String text) {
        return text != null && !text.isEmpty();
    }

}
