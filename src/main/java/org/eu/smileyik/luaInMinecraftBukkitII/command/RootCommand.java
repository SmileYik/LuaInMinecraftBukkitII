package org.eu.smileyik.luaInMinecraftBukkitII.command;

import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.ILuaStateEnvInner;
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
            description = "Call lua closure",
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

    @Command(
            value = "reload",
            description = "Reload Configuration"
    )
    public void reload(CommandSender sender) throws Exception {
        sender.sendMessage("§eNotice: If you wanna change lua version, you must restart server. ");
        LuaInMinecraftBukkit.instance().reload();
        sender.sendMessage("Reloaded Configuration");
    }

    @Command(
            value = "reloadEnv",
            args = {"lua_env"},
            description = "Reload Lua Environment"
    )
    public void reloadEnv(CommandSender sender, String[] args) throws Exception {
        String luaEnv = args[0];
        ILuaStateEnv env = LuaInMinecraftBukkit.instance().getLuaStateManager().getEnv(luaEnv);
        if (env == null) {
            sender.sendMessage("Lua Environment '" + luaEnv + "' not found");
            return;
        }
        ((ILuaStateEnvInner) env).reload();
        sender.sendMessage("Reloaded Lua Environment");
    }

    @Command(
            value = "softReloadEnv",
            args = {"lua_env"},
            description = "Soft Reload Lua Environment"
    )
    public void softReloadEnv(CommandSender sender, String[] args) throws Exception {
        String luaEnv = args[0];
        ILuaStateEnv env = LuaInMinecraftBukkit.instance().getLuaStateManager().getEnv(luaEnv);
        if (env == null) {
            sender.sendMessage("Lua Environment '" + luaEnv + "' not found");
            return;
        }
        ((ILuaStateEnvInner) env).softReload();
        sender.sendMessage("Soft Reloaded Lua Environment");
    }
}
