package org.eu.smileyik.luaInMinecraftBukkitII.command;

import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageJsonMeta;
import org.eu.smileyik.simplecommand.annotation.Command;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Command(
        value = "Luacage",
        permission = "LuaInMinecraftBukkitII.Admin"
)
public class LuacageCommand {
    @Command(
            value = "update",
            description = "Update package index"
    )
    public void update(CommandSender sender, String[] args) {
        getLuaState(sender, env -> {
            env.getLuacage().update();
            sender.sendMessage("Luacage updated");
        });
    }

    @Command(
            value = "list",
            args = {"page"},
            description = "List packages"
    )
    public void list(CommandSender sender, String[] args) {
        final int pageSize = 5;
        getLuaState(sender, env -> {
            int page = 1;
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid page number");
            }

            List<LuacageJsonMeta> packages = env.getLuacage().getPackages();
            int size = packages.size();
            int pageCount = size / pageSize + (size % pageSize == 0 ? 0 : 1);
            if (page > pageCount) {
                page = pageCount;
            } else if (page < 1) {
                page = 1;
            }
            StringBuilder msg = new StringBuilder();
            packages.subList((page - 1) * pageSize, page * pageSize).forEach(pkg -> {

            });

            sender.sendMessage("Luacage updated");
        });
    }

    protected boolean getLuaState(CommandSender sender, Consumer<ILuaStateEnv> consumer) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        ILuaStateManager manager = plugin.getLuaStateManager();
        if (manager == null) {
            sender.sendMessage("No Lua state manager available");
            return false;
        }
        Optional<ILuaStateEnv> result = manager.getScriptEnvs().stream().findFirst();
        if (!result.isPresent()) {
            sender.sendMessage("No Lua state environment available");
            return false;
        }
        consumer.accept(result.get());
        return true;
    }
}
