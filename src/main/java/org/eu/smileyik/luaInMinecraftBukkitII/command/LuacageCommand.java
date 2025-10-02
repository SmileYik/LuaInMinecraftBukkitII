package org.eu.smileyik.luaInMinecraftBukkitII.command;

import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.ILuacageRepository;
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
            sender.sendMessage("Updating Luacage index... It will take a while...");
            LuaInMinecraftBukkit.instance().getScheduler().runTaskAsynchronously(LuaInMinecraftBukkit.instance(), () -> {
                env.getLuacage().update();
                sender.sendMessage("Luacage updated");
            });
        });
    }

    @Command(
            value = "list",
            description = "List packages"
    )
    public void listFirstPage(CommandSender sender, String[] args) {
        list(sender, new String[] { "1" });
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
            }
            if (page < 1) {
                page = 1;
            }
            int numLen = Integer.toString(pageCount).length();
            int i = (page - 1) * pageSize + 1;
            List<LuacageJsonMeta> result = packages.subList((page - 1) * pageSize, Math.min(page * pageSize, size));
            String msg = generatePackageInformation(i, result);

            sender.sendMessage(String.format("\n" +
                    "Page %" + numLen + "d of %" + numLen + "d:\n%s",
                    page, pageCount, msg
            ));
        });
    }

    @Command(
            value = "search",
            args = {"keyword"},
            description = "Search package"
    )
    public void search(CommandSender sender, String[] args) {
        getLuaState(sender, env -> {
            String msg = generatePackageInformation(1, env.getLuacage().findPackages(args[0]));
            sender.sendMessage("Search result:\n" + msg);
        });
    }

    @Command(
            value = "installed",
            description = "List installed packages"
    )
    public void installed(CommandSender sender, String[] args) {
        getLuaState(sender, env -> {
            List<LuacageJsonMeta> installed = env.getLuacage().installedPackages();
            if (installed.isEmpty()) {
                sender.sendMessage("No installed packages.");
            } else {
                sender.sendMessage("Installed " + installed.size() + " packages:\n" +
                        generatePackageInformation(1, installed));
            }
        });
    }

    @Command(
            value = "install",
            args = "package-name",
            description = "Install package"
    )
    public void install(CommandSender sender, String[] args) {
        getLuaState(sender, env -> {
            String packageName = args[0];
            sender.sendMessage("Installing " + packageName + "... It will take a while...");
            LuaInMinecraftBukkit.instance().getScheduler().runTaskAsynchronously(LuaInMinecraftBukkit.instance(), () -> {
                List<LuacageJsonMeta> results = env.getLuacage().findPackages(packageName, null, ILuacageRepository.SEARCH_TYPE_PKG_NAME_EXACTLY);
                if (results.isEmpty()) {
                    sender.sendMessage("No packages named " + packageName);
                    return;
                } else if (results.size() > 1) {
                    String msg = generatePackageInformation(1, results);
                    sender.sendMessage("Multiple packages named " + packageName + ":\n" + msg);
                    return;
                }
                LuacageJsonMeta pkg = results.get(0);
                env.getLuacage().installPackage(pkg, true);
                sender.sendMessage("Finished installing " + packageName + ".");
            });
        });
    }

    protected String generatePackageInformation(int startIdx, List<LuacageJsonMeta> packages) {
        int len = String.valueOf(startIdx + packages.size()).length();
        StringBuilder msg = new StringBuilder();
        for (LuacageJsonMeta pkg : packages) {
            msg.append(String.format("\n" +
                    "%" + len + "d. %s/%s %s:\n" +
                    "  %s",
                    startIdx++, pkg.getSource(), pkg.getName(), pkg.getVersion(),
                    pkg.getDescription()));
        }
        return msg.length() == 0 ? "" : msg.substring(1);
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
