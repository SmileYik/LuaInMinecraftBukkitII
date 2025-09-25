package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LuacageConfig {
    private static final Source DEFAULT_SOURCE = new Source();

    static {
        DEFAULT_SOURCE.name = "default";
        DEFAULT_SOURCE.url = "https://raw.githubusercontent.com/SmileYik/luacage-demo/refs/heads/master/";
    }

    private boolean enable = true;
    private Source[] sources;

    public Source[] getSources() {
        if (sources == null) {
            sources = new Source[]{DEFAULT_SOURCE};
            return sources;
        }
        for (Source source : sources) {
            if (source != null && source.url != null && !source.url.endsWith("/")) {
                source.url += "/";
            }
        }
        return sources;
    }

    @Data
    @ToString
    public static final class Source {
        private String name;
        private String url;
    }
}
