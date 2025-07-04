package org.eu.smileyik.luaInMinecraftBukkitII.luaState.event;

import lombok.Data;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eu.smileyik.luaInMinecraftBukkitII.LuaInMinecraftBukkit;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.ILuaEnv;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event.ILuaEventListenerBuilder;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event.LuaEventListenerProperty;
import org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState.event.LuaUnregisteredListener;
import org.eu.smileyik.luaInMinecraftBukkitII.reflect.LuaTable2Object;
import org.eu.smileyik.luajava.type.ILuaCallable;
import org.eu.smileyik.luajava.type.LuaTable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LuaEventListenerBuilder implements ILuaEventListenerBuilder {
    private static final String[] EVENT_CLASS_PREFIX = {
            "org.bukkit.event.",
            "org.bukkit.event.player.",
            "org.bukkit.event.event.",
            "org.bukkit.event.world.",
            "org.bukkit.event.inventory.",
    };
    private final List<EventConfig> eventConfigs = new LinkedList<>();
    private final ILuaEnv luaEnv;

    public LuaEventListenerBuilder(ILuaEnv luaEnv) {
        this.luaEnv = luaEnv;
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                              @NotNull ILuaCallable closure) throws ClassNotFoundException {
        Class<?> eventClass = findEventClass(eventClassName);
        return this.doSubscribe(eventClass, closure, null, null);
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                              @NotNull EventPriority eventPriority,
                                              @NotNull ILuaCallable closure) throws ClassNotFoundException {
        Class<?> eventClass = findEventClass(eventClassName);
        return this.doSubscribe(eventClass, closure, eventPriority, null);
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                              @NotNull String eventPriority,
                                              @NotNull ILuaCallable closure) throws ClassNotFoundException {
        EventPriority priority = EventPriority.valueOf(eventPriority.toUpperCase());
        return subscribe(eventClassName, priority, closure);
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                              @NotNull EventPriority eventPriority,
                                              boolean ignoreCancelled,
                                              @NotNull ILuaCallable closure) throws ClassNotFoundException {
        Class<?> eventClass = findEventClass(eventClassName);
        return this.doSubscribe(eventClass, closure, eventPriority, ignoreCancelled);
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                              @NotNull String eventPriority,
                                              boolean ignoreCancelled,
                                              @NotNull ILuaCallable closure) throws ClassNotFoundException {
        EventPriority priority = EventPriority.valueOf(eventPriority.toUpperCase());
        return this.subscribe(eventClassName, priority, ignoreCancelled, closure);
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull String eventClassName,
                                              boolean ignoreCancelled,
                                              @NotNull ILuaCallable closure) throws ClassNotFoundException {
        Class<?> eventClass = findEventClass(eventClassName);
        return this.doSubscribe(eventClass, closure, null, ignoreCancelled);
    }

    @Override
    public ILuaEventListenerBuilder subscribe(@NotNull LuaTable table) throws Exception {
        LuaEventListenerProperty property = LuaTable2Object.covert(table, LuaEventListenerProperty.class)
                .getOrThrow();
        if (property.getEvent() == null) {
            throw new IllegalArgumentException("'event' property must not be nil'");
        }
        if (property.getHandler() == null) {
            throw new IllegalArgumentException("'handler' property must not be nil'");
        }
        Class<?> eventClass = findEventClass(property.getEvent());
        ILuaCallable callable = property.getHandler();
        boolean ignoreCancelled = property.isIgnoreCancelled();
        EventPriority priority;
        if (property.getPriority() == null) {
            priority = null;
        } else {
            priority = EventPriority.valueOf(property.getPriority());
        }
        return this.doSubscribe(eventClass, callable, priority, ignoreCancelled);
    }

    @Override
    public ILuaEventListenerBuilder subscribes(@NotNull LuaTable... tables) throws Exception {
        for (LuaTable t : tables) {
            subscribe(t);
        }
        return this;
    }

    private ILuaEventListenerBuilder doSubscribe(Class<?> eventClass, ILuaCallable closure,
                                                 EventPriority eventPriority, Boolean ignoreCancelled) {
        this.eventConfigs.add(new EventConfig(eventClass, closure, eventPriority, ignoreCancelled));
        return this;
    }

    @Override
    public LuaUnregisteredListener build()
            throws NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        int count = 0;
        DynamicType.Builder<LuaEventListener> byteBuddy = new ByteBuddy()
                .subclass(LuaEventListener.class)
                .constructor(ElementMatchers.any())
                .intercept(MethodCall.invoke(LuaEventListener.class.getConstructor(LuaEventHandler.class))
                        .withArgument(0));
        Map<String, ILuaCallable> callableMap = new HashMap<>();
        LuaEventHandler luaEventHandler = new LuaEventHandler(callableMap);

        for (EventConfig eventConfig : this.eventConfigs) {
            AnnotationDescription.Builder builder = AnnotationDescription.Builder.ofType(EventHandler.class);
            if (eventConfig.eventPriority != null) {
                builder = builder.define("priority", eventConfig.eventPriority);
            }
            if (eventConfig.ignoreCancelled != null) {
                builder = builder.define("ignoreCancelled", eventConfig.ignoreCancelled);
            }
            AnnotationDescription eventHandler = builder.build();

            String methodName = String.format("on%s%d", eventConfig.eventClass.getSimpleName(), count++);
            callableMap.put(methodName, eventConfig.closure);
            byteBuddy = byteBuddy.defineMethod(methodName, void.class, Visibility.PUBLIC)
                    .withParameters(eventConfig.eventClass)
                    .intercept(MethodDelegation.to(luaEventHandler))
                    .annotateMethod(eventHandler);
        }
        try (DynamicType.Unloaded<LuaEventListener> made = byteBuddy.make()) {
            Listener listener = made
                    .load(LuaInMinecraftBukkit.instance().classLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor(LuaEventHandler.class)
                    .newInstance(luaEventHandler);
            return new LuaUnregisteredListener(luaEnv, listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            eventConfigs.clear();
        }
    }

    @NotNull
    private static Class<?> findEventClass(String eventClassName) throws ClassNotFoundException {
        Class<?> eventClass = null;
        try {
            eventClass = Class.forName(eventClassName);
        } catch (ClassNotFoundException e) {
            for (String prefix : EVENT_CLASS_PREFIX) {
                try {
                    eventClass = Class.forName(prefix + eventClassName);
                    break;
                } catch (ClassNotFoundException ignore) {
                }
            }
            if (eventClass == null) {
                throw e;
            }
        }
        return eventClass;
    }

    @Data
    public static class EventConfig {
        private final Class<?> eventClass;
        private final ILuaCallable closure;
        private final EventPriority eventPriority;
        private final Boolean ignoreCancelled;
    }
}
