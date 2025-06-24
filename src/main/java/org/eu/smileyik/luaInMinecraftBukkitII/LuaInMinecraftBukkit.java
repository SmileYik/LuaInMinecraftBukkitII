package org.eu.smileyik.luaInMinecraftBukkitII;

import lombok.Getter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.concurrent.Callable;

public final class LuaInMinecraftBukkit extends JavaPlugin {

    private static final String[] FOLDERS = new String[] {
            "luastate",
            "natives",
            "scripts"
    };

    private static LuaInMinecraftBukkit plugin;

    public LuaInMinecraftBukkit() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        for (String folder : FOLDERS) {
            if (!new File(getDataFolder(), folder).exists()) {
                new File(getDataFolder(), folder).mkdirs();
            }
        }
        if (!new File(getDataFolder(), "config.json").exists()) {
            saveResource("config.json", false);
        }

        AnnotationDescription annotationDescription = AnnotationDescription.Builder
                .ofType(EventHandler.class)
                .define("priority", EventPriority.HIGHEST)
                .define("ignoreCancelled", false)
                .build();

        Class<?> onEvent = new ByteBuddy()
                .subclass(Object.class)
                .implement(Listener.class)
                .defineMethod("onEvent", Void.class, Visibility.PUBLIC)
                    .withParameters(PlayerJoinEvent.class)
                    .intercept(MethodDelegation.to(new A()))
                    .annotateMethod(annotationDescription)
                .make()
                .load(getClassLoader())
                .getLoaded();
        try {
            Object o = onEvent.getDeclaredConstructor().newInstance();
            getServer().getPluginManager().registerEvents((Listener) o, this);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static LuaInMinecraftBukkit instance() {
        return plugin;
    }

    public static final class A {
        @RuntimeType
        public Object intercept(@Argument(0) Object event,
                                @Origin Method method) {
            System.out.println("Intercepted " + method.getName() + " : " + event);
            return null;
        }
    }
}
