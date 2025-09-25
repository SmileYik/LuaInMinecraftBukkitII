package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LuacageRepositoryBundle extends ArrayList<ILuacageRepository> implements ILuacageRepository {

    public LuacageRepositoryBundle(int size) {
        super(size);
    }

    public LuacageRepositoryBundle() {

    }

    @Override
    public Collection<LuacageJsonMeta> getPackages() {
        return parallelStream()
                .flatMap(it -> it.getPackages().parallelStream())
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<LuacageJsonMeta> findPackages(String packageName, String desc, short searchType) {
        return parallelStream()
                .flatMap(it -> it.findPackages(packageName, desc, searchType).parallelStream())
                .collect(Collectors.toList());
    }
}
