package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ConstructorGenerator extends MemberGenerator {
    public static final ConstructorGenerator INSTANCE = new ConstructorGenerator();

    @Override
    protected Class<?> implementationClass() {
        return ExecutorAccessor.class;
    }

    public ExecutorAccessor generate(final Constructor<?> constructor) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> declaringClass = constructor.getDeclaringClass();
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        try (DynamicType.Unloaded<Object> invoke = generateClass(declaringClass)
                .defineField("handle", MethodHandle.class, Visibility.PRIVATE, Ownership.STATIC, SyntheticState.SYNTHETIC, FieldManifestation.FINAL)
                .invokable(MethodDescription::isTypeInitializer)
                .intercept(new Implementation() {
                    @Override
                    public ByteCodeAppender appender(Target implementationTarget) {
                        return (methodVisitor, implementationContext, instrumentedMethod) -> {
                            getTargetConstructor(constructor, implementationTarget, methodVisitor, implementationContext);
                            try {
                                getAndSetMethodHandle(Constructor.class, implementationTarget, methodVisitor, implementationContext);
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
                                generateInvoke(constructor, implementationTarget, methodVisitor, implementationContext, instrumentedMethod);
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

    protected void getTargetConstructor(Constructor<?> targetMethod,
                                        Implementation.Target implementationTarget,
                                        MethodVisitor methodVisitor,
                                        Implementation.Context implementationContext) {
        ClassConstant.of(TypeDescription.ForLoadedType.of(targetMethod.getDeclaringClass())).apply(methodVisitor, implementationContext);
        generateClassArray(methodVisitor, implementationContext, targetMethod.getParameterTypes());
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredConstructor",
                "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
                false
        );
    }
}
