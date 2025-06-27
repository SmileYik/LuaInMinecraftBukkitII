package org.eu.smileyik.luaInMinecraftBukkitII.command;

import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.simplecommand.annotation.Command;

@Command(
        value = "LuaInMinecraftBukkitII",
        aliases = {"lua", "limb", "limb2"},
        permission = "LuaInMinecraftBukkitII.Admin"
)
public class RootCommand {
    @Command(
            value = "call",
            args = {"env", "closure_name", "..."},
            description = "调用指定环境中的Lua方法",
            isUnlimitedArgs = true
    )
    public void call(CommandSender sender, String[] args) throws Exception {
        if (args.length < 2) {
            sender.sendMessage("你给予的参数不正确哦");
            return;
        }
        String env = args[0];
        String closureName = args[1];
        ILuaStateEnv luaEnv = LuaInMinecraftBukkit.instance().getLuaStateManager().getEnv(env);
        if (luaEnv == null) {
            sender.sendMessage(String.format("环境 '%s' 不存在", env));
            return;
        }
        String[] params = new String[args.length - 2];
        System.arraycopy(args, 2, params, 0, args.length - 2);
        luaEnv.callClosure(closureName, (Object[]) params).justThrow();
    }
}
