package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class MethodGenerator extends MemberGenerator {

    public static final MethodGenerator INSTANCE = new MethodGenerator();

    @Override
    protected Class<?> implementationClass() {
        return ExecutorAccessor.class;
    }

    public ExecutorAccessor generate(Method method) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?>[] parameterTypes = method.getParameterTypes();

        try (DynamicType.Unloaded<Object> invoke = generateClass(declaringClass)
                .defineField("handle", MethodHandle.class, Visibility.PRIVATE, Ownership.STATIC, SyntheticState.SYNTHETIC, FieldManifestation.FINAL)
                .invokable(MethodDescription::isTypeInitializer)
                .intercept(new Implementation() {
                    @Override
                    public ByteCodeAppender appender(Target implementationTarget) {
                        return (methodVisitor, implementationContext, instrumentedMethod) -> {
                            getTargetMethod(method, implementationTarget, methodVisitor, implementationContext);
                            try {
                                getAndSetMethodHandle(Method.class, implementationTarget, methodVisitor, implementationContext);
                            } catch (NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }

                            methodVisitor.visitInsn(Opcodes.RETURN);
                            return new ByteCodeAppender.Size(parameterTypes.length * 3 + 3, instrumentedMethod.getStackSize());
                        };
                    }

                    @Override
                    public InstrumentedType prepare(InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }
                })
                .method(named("invoke"))
                .intercept(new Implementation() {
                    @Override
                    public ByteCodeAppender appender(Target implementationTarget) {
                        return (methodVisitor, implementationContext, instrumentedMethod) ->
                                generateInvoke(method, implementationTarget, methodVisitor, implementationContext, instrumentedMethod);
                    }

                    @Override
                    public InstrumentedType prepare(InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }
                })
                .make()) {
            return (ExecutorAccessor) invoke
                    .load(MethodGenerator.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        }

    }
}
