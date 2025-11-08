package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class LuacageConfig {
    private static final Source DEFAULT_SOURCE = new Source();

    static {
        DEFAULT_SOURCE.name = "default";
        DEFAULT_SOURCE.url = "https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII-Luacage/refs/heads/repo/";
    }

    private boolean enable = true;
    private Source[] sources;

    public Source[] getSources() {
        if (sources == null) {
            sources = new Source[]{DEFAULT_SOURCE};
            return sources;
        }
        Set<String> checked = new HashSet<>();
        List<Source> list = new ArrayList<>();
        for (Source source : sources) {
            if (source == null || source.name == null || source.url == null || Objects.equals("local", source.name)) {
                continue;
            }
            if (checked.add(source.name)) {
                String url = source.url;
                if (!url.endsWith("/")) {
                    source.url += "/";
                }
                list.add(source);
            }
        }
        if (!checked.contains("default")) {
            list.add(DEFAULT_SOURCE);
        }
        return list.toArray(new Source[0]);
    }

    @Data
    @ToString
    public static final class Source {
        private String name;
        private String url;
    }
}
