package org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class LuacageLuaMeta extends LuacageCommonMeta {

    /**
     * main lua file of. if set null then means no main script files.
     */
    private String main;

    /**
     * is runnable.
     */
    private boolean runnable;

    /**
     * the reason if not runnable.
     */
    private String reason;
}
