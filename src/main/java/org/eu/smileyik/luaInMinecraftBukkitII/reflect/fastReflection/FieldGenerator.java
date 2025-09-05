package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import net.bytebuddy.description.field.FieldDescription;
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
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class FieldGenerator extends MemberGenerator {

    public static final FieldGenerator INSTANCE = new FieldGenerator();

    @Override
    protected Class<?> implementationClass() {
        return FieldAccessor.class;
    }

    public FieldAccessor generate(Field field) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<Object> builder = generateClass(field.getDeclaringClass())
                .defineField("setter", MethodHandle.class, Visibility.PRIVATE, Ownership.STATIC, SyntheticState.SYNTHETIC, FieldManifestation.FINAL)
                .defineField("getter", MethodHandle.class, Visibility.PRIVATE, Ownership.STATIC, SyntheticState.SYNTHETIC, FieldManifestation.FINAL)
                .invokable(MethodDescription::isTypeInitializer)
                .intercept(new Implementation() {
                    @Override
                    public ByteCodeAppender appender(Target implementationTarget) {
                        try {
                            return staticBlockBody(field, implementationTarget);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public InstrumentedType prepare(InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }
                })
                .method(named("get"))
                .intercept(new Implementation() {
                    @Override
                    public ByteCodeAppender appender(Target implementationTarget) {
                        return generateGetBody(field, implementationTarget);
                    }

                    @Override
                    public InstrumentedType prepare(InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }
                });
        if (!Modifier.isFinal(field.getModifiers())) {
            builder = builder.method(named("set"))
                    .intercept(new Implementation() {
                        @Override
                        public ByteCodeAppender appender(Target implementationTarget) {
                            return generateSetBody(field, implementationTarget);
                        }

                        @Override
                        public InstrumentedType prepare(InstrumentedType instrumentedType) {
                            return instrumentedType;
                        }
                    });
        }
        try (DynamicType.Unloaded<Object> make = builder.make()) {
            return (FieldAccessor) make
                    .load(FieldAccessor.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        }
    }

    protected ByteCodeAppender generateGetBody(Field field, Implementation.Target implementationTarget) {
        Class<?> declaringClass = field.getDeclaringClass();
        Class<?> type = field.getType();
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        FieldDescription.InDefinedShape handle = implementationTarget.getInstrumentedType()
                .getDeclaredFields()
                .filter(named("getter"))
                .getOnly();

        return (methodVisitor, implementationContext, instrumentedMethod) -> {
            methodVisitor.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    handle.getDeclaringType().getInternalName(),
                    handle.getName(),
                    handle.getDescriptor()
            );

            if (!isStatic) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, declaringClass.getName().replace(".", "/"));
            }

            methodVisitor.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/invoke/MethodHandle",
                    "invokeExact",
                    generateGetterInvokeDescription(field),
                    false
            );
            if (type.isPrimitive()) {
                generateBoxedTypeFrom(methodVisitor, type);
            }
            // methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, targetMethod.getReturnType().getName().replace('.', '/'));
            methodVisitor.visitInsn(Opcodes.ARETURN);

            return new ByteCodeAppender.Size(2, instrumentedMethod.getStackSize());
        };
    }

    protected ByteCodeAppender generateSetBody(Field field, Implementation.Target implementationTarget) {
        Class<?> declaringClass = field.getDeclaringClass();
        Class<?> type = field.getType();
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        FieldDescription.InDefinedShape handle = implementationTarget.getInstrumentedType()
                .getDeclaredFields()
                .filter(named("setter"))
                .getOnly();

        return (methodVisitor, implementationContext, instrumentedMethod) -> {
            methodVisitor.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    handle.getDeclaringType().getInternalName(),
                    handle.getName(),
                    handle.getDescriptor()
            );

            if (!isStatic) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, declaringClass.getName().replace(".", "/"));
            }

            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            if (type.isPrimitive()) {
                generateToUnboxType(methodVisitor, TypeDescription.ForLoadedType.of(type));
            } else {
                methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, type.getName().replace(".", "/"));
            }

            methodVisitor.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/invoke/MethodHandle",
                    "invokeExact",
                    generateSetterInvokeDescription(field),
                    false
            );
            methodVisitor.visitInsn(Opcodes.RETURN);
            return new ByteCodeAppender.Size(3, instrumentedMethod.getStackSize());
        };
    }

    protected ByteCodeAppender staticBlockBody(Field field, Implementation.Target implementationTarget) throws NoSuchMethodException {
        Class<?> declaringClass = field.getDeclaringClass();
        FieldDescription.InDefinedShape getterHandle = implementationTarget.getInstrumentedType()
                .getDeclaredFields()
                .filter(named("getter"))
                .getOnly();
        FieldDescription.InDefinedShape setterHandle = implementationTarget.getInstrumentedType()
                .getDeclaredFields()
                .filter(named("setter"))
                .getOnly();
        Method getterMethod = Lookup.class.getDeclaredMethod("getFieldGetter", Field.class);
        Method setterMethod = Lookup.class.getDeclaredMethod("getFieldSetter", Field.class);

        return (methodVisitor, implementationContext, instrumentedMethod) -> {
            ClassConstant.of(TypeDescription.ForLoadedType.of(declaringClass)).apply(methodVisitor, implementationContext);
            new TextConstant(field.getName()).apply(methodVisitor, implementationContext);
            methodVisitor.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class",
                    "getDeclaredField",
                    "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                    false
            );
            methodVisitor.visitInsn(Opcodes.DUP);

            methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    Lookup.class.getName().replace('.', '/'),
                    getterMethod.getName(),
                    Type.getMethodDescriptor(getterMethod),
                    false
            );
            methodVisitor.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    getterHandle.getDeclaringType().getInternalName(),
                    getterHandle.getName(),
                    getterHandle.getDescriptor()
            );

            methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    Lookup.class.getName().replace('.', '/'),
                    setterMethod.getName(),
                    Type.getMethodDescriptor(setterMethod),
                    false
            );
            methodVisitor.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    setterHandle.getDeclaringType().getInternalName(),
                    setterHandle.getName(),
                    setterHandle.getDescriptor()
            );

            methodVisitor.visitInsn(Opcodes.RETURN);
            return new ByteCodeAppender.Size(2, instrumentedMethod.getStackSize());
        };
    }
}
