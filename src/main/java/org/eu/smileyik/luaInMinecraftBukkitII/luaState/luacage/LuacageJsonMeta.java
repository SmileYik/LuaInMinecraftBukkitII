package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class LuacageJsonMeta extends LuacageCommonMeta {
    /**
     * package file paths. need starts with '/'.
     */
    private String[] files;

    /**
     * hashes of files. length need as same as files field.
     */
    private String[] hashes;

    /**
     * updated timestamp
     */
    private Long updateAt;

    /**
     * created timestamp
     */
    private Long createdAt;

    /**
     * from which repo.
     */
    private String source;
}
