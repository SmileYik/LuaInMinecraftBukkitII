package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaTest {
    @Test
    public void runnableTest() throws Throwable {
        Runnable runnable = () -> {
            System.out.println("Lambda test");
        };
        ExecutorAccessor accessor = MethodGenerator.INSTANCE.generate(runnable.getClass().getDeclaredMethod("run"));
        accessor.invoke(runnable);
    }

    @Test
    public void consumerTest() throws Throwable {
        Consumer<String> consumer = (String s) -> {
            System.out.println("Lambda test " + s);
        };

        ExecutorAccessor accessor = MethodGenerator.INSTANCE.generate(consumer.getClass().getDeclaredMethod("accept", Object.class));
        accessor.invoke(consumer, "abc");
    }

    @Test
    public void functionTest() throws Throwable {
        Function<Integer, String> function = (a) -> "" + a;
        ExecutorAccessor accessor = MethodGenerator.INSTANCE.generate(function.getClass().getDeclaredMethod("apply", Object.class));
        System.out.println(accessor.invoke(function, 128));
    }

    @Test
    public void biFunctionTest() throws Throwable {
        BiFunction<Integer, Integer, Integer> function = (a, b) -> a + b;
        ExecutorAccessor accessor = MethodGenerator.INSTANCE.generate(function.getClass().getDeclaredMethod("apply", Object.class, Object.class));
        System.out.println(accessor.invoke(function, 1, 1));
    }

}
