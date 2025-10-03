package org.eu.smileyik.luaInMinecraftBukkitII.command;

import org.bukkit.command.CommandSender;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.ILuaStateManager;
import org.eu.smileyik.luaInMinecraftBukkitII.api.luaState.ILuaStateEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.command.LuaCommandRegister;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.ILuacage;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.ILuacageRepository;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageCommonMeta;
import org.eu.smileyik.luaInMinecraftBukkitII.luaState.luacage.LuacageJsonMeta;
import org.eu.smileyik.simplecommand.CommandService;
import org.eu.smileyik.simplecommand.TabSuggest;
import org.eu.smileyik.simplecommand.annotation.Command;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.InvalidClassException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            String msg = generatePackageInformation(env.getLuacage(), i, result);

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
            String msg = generatePackageInformation(env.getLuacage(), 1, env.getLuacage().findPackages(args[0]));
            sender.sendMessage("Search result:\n" + msg);
        });
    }

    @Command(
            value = "installed",
            args = "lua-env",
            description = "List installed packages"
    )
    public void installed(CommandSender sender, String[] args) {
        getLuaState(args[0], sender, env -> {
            List<LuacageJsonMeta> installed = env.getLuacage().installedPackages();
            if (installed.isEmpty()) {
                sender.sendMessage("No installed packages.");
            } else {
                sender.sendMessage("Installed " + installed.size() + " packages:\n" +
                        generatePackageInformation(env.getLuacage(), 1, installed));
            }
        });
    }

    @Command(
            value = "install",
            args = {"lua-env", "package-name"},
            description = "Install package"
    )
    public void install(CommandSender sender, String[] args) {
        getLuaState(args[0], sender, env -> {
            String source = null;
            String packageFullName = args[1];
            String packageName = packageFullName;
            if (packageName.contains("/")) {
                int idx  = packageName.indexOf("/");
                source = packageName.substring(0, idx);
                packageName = packageName.substring(idx + 1);
            }

            String fSource = source;
            String fPackageName = packageName;
            sender.sendMessage("Installing " + packageName + "... It will take a while...");
            LuaInMinecraftBukkit.instance().getScheduler().runTaskAsynchronously(LuaInMinecraftBukkit.instance(), () -> {
                List<LuacageJsonMeta> results = env.getLuacage().findPackages(fPackageName, null, ILuacageRepository.SEARCH_TYPE_PKG_NAME_EXACTLY);
                LuacageJsonMeta target;
                if (results.isEmpty()) {
                    sender.sendMessage("No packages named " + fPackageName);
                    return;
                } else if (results.size() > 1) {
                    if (fSource == null) {
                        String msg = generatePackageInformation(env.getLuacage(), 1, results);
                        sender.sendMessage("Multiple packages named " + fPackageName + ":\n" + msg);
                        return;
                    }
                    target = results.stream()
                            .filter(it -> Objects.equals(it.getSource(), fSource))
                            .findFirst()
                            .orElse(null);
                    if (target == null) {
                        String msg = generatePackageInformation(env.getLuacage(), 1, results);
                        sender.sendMessage("No packages named " + packageFullName + ":\n" + msg);
                        return;
                    }
                } else {
                    target = results.get(0);
                }
                try {
                    env.getLuacage().installPackage(target, true);
                    sender.sendMessage("Finished installing " + packageFullName + ".");
                } catch (Exception e) {
                    sender.sendMessage("Failed to install package `" + fPackageName + "`: " + e);
                    DebugLogger.debug(e);
                }
            });
        });
    }

    @Command(
            value = "uninstall",
            args = {"lua-env", "package-name"},
            description = "Uninstall package"
    )
    public void uninstall(CommandSender sender, String[] args) {
        getLuaState(args[0], sender, env -> {
            String packageFullName = args[1];
            String packageName = packageFullName.contains("/") ? packageFullName.substring(0, packageFullName.indexOf("/")) : packageFullName;
            env.getLuacage()
                    .installedPackages()
                    .stream()
                    .filter(it -> Objects.equals(it.getName(), packageName))
                    .findAny()
                    .ifPresent(it -> {
                        env.getLuacage().uninstallPackage(it);
                    });
            sender.sendMessage("Uninstalled " + packageFullName + ".");
        });
    }

    @Command(
            value = "autoRemove",
            args = "lua-env",
            description = "Auto remove useless packages"
    )
    public void autoRemove(CommandSender sender, String[] args) {
        getLuaState(args[0], sender, env -> {
            List<LuacageJsonMeta> removed = env.getLuacage().removeUselessPackages();
            if (removed.isEmpty()) {
                sender.sendMessage("No useless packages found.");
            } else {
                String msg = generatePackageInformation(env.getLuacage(), 1, removed);
                sender.sendMessage(String.format(
                        "Removed %s packages:\n%s",  removed.size(), msg
                ));
            }
        });
    }

    protected String generatePackageInformation(ILuacage luacage, int startIdx, List<LuacageJsonMeta> packages) {
        int len = String.valueOf(startIdx + packages.size()).length();
        StringBuilder msg = new StringBuilder();
        for (LuacageJsonMeta pkg : packages) {
            boolean installed = luacage.installedPackages().parallelStream().anyMatch(it -> Objects.equals(it.getName(), pkg.getName()));
            msg.append(String.format("\n" +
                    "%" + len + "d. %s/%s %s %s:\n" +
                    "  %s",
                    startIdx++, pkg.getSource(), pkg.getName(), pkg.getVersion(),
                    installed ? "§2[§nInstalled]§r" : "",
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

    protected boolean getLuaState(String envId, CommandSender sender, Consumer<ILuaStateEnv> consumer) {
        LuaInMinecraftBukkit plugin = LuaInMinecraftBukkit.instance();
        ILuaStateManager manager = plugin.getLuaStateManager();
        if (manager == null) {
            sender.sendMessage("No Lua state manager available");
            return false;
        }
        ILuaStateEnv env = manager.getEnv(envId);
        if (env == null) {
            sender.sendMessage("No Lua state environment available: " + envId);
            return false;
        }
        consumer.accept(env);
        return true;
    }

    public static void register(LuaInMinecraftBukkit plugin) throws InvalidClassException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CommandService commandService = CommandService.newInstance(
                LuaCommandRegister.DEFAULT_TRANSLATOR,
                LuaCommandRegister.DEFAULT_FORMAT,
                LuacageCommand.class
        );
        commandService.registerTabSuggest(new TabSuggest() {
            @Override
            public String getKeyword() {
                return "lua-env";
            }

            @Override
            public List<String> suggest() {
                return new ArrayList<>(plugin.getLuaStateManager().getScriptEnvIds());
            }
        });
        commandService.registerTabSuggest(new TabSuggest() {
            @Override
            public String getKeyword() {
                return "package-name";
            }

            @Override
            public List<String> suggest(String[] args, int commandIdx) {
                String envId = args[1];
                ILuaStateEnv env = plugin.getLuaStateManager().getEnv(envId);
                List<String> suggestions = Collections.emptyList();
                if (env != null) {
                    String command = args[0];
                    if ("install".equalsIgnoreCase(command)) {
                        suggestions = env.getLuacage()
                                .getPackages()
                                .stream()
                                .map(it -> it.getSource() + "/" + it.getName())
                                .collect(Collectors.toList());
                    } else if ("uninstall".equalsIgnoreCase(command)) {
                        suggestions = env.getLuacage()
                                .installedPackages()
                                .stream()
                                .map(LuacageCommonMeta::getName)
                                .collect(Collectors.toList());
                    }
                }
                return suggestions;
            }
        });
        commandService.registerToBukkit(plugin);
    }
}
