package org.eu.smileyik.luaInMinecraftBukkitII.config;

import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Data
@ToString
public class NativeLibraryConfig {
    /**
     * mirrors
     */
    private String[] urls;

    /**
     * native files
     */
    private Map<String, Map<String, Map<String, String[]>>> files;

    /**
     * native modules. like files, but not require.
     */
    private Map<String, Map<String, Map<String, String[]>>> modules;

    public Collection<String> systems() {
        return files.keySet();
    }

    public Collection<String> architectures(String system) {
        return files.getOrDefault(system, Collections.emptyMap()).keySet();
    }

    public Collection<String> versions(String system, String architecture) {
        return files.getOrDefault(system, Collections.emptyMap())
                .getOrDefault(architecture, Collections.emptyMap())
                .keySet();
    }

    @Nullable
    public String[] version(String system, String architecture, String version) {
        return files.getOrDefault(system, Collections.emptyMap())
                .getOrDefault(architecture, Collections.emptyMap())
                .get(version);
    }

    /**
     * available module collection.
     * @param system       target system
     * @param architecture target arch
     * @return available modules
     */
    public Collection<String> availableModules(String system, String architecture) {
        return modules.getOrDefault(system, Collections.emptyMap())
                .getOrDefault(architecture, Collections.emptyMap())
                .keySet();
    }

    /**
     * get module's native files
     */
    @Nullable
    public String[] module(String system, String architecture, String module) {
        return modules.getOrDefault(system, Collections.emptyMap())
                .getOrDefault(architecture, Collections.emptyMap())
                .get(module);
    }
}
