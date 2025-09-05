package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import org.eu.smileyik.luajava.reflect.ReflectUtil;

import java.lang.reflect.*;

import static net.bytebuddy.matcher.ElementMatchers.named;

public abstract class MemberGenerator {

    protected abstract Class<?> implementationClass();

    protected DynamicType.Builder<Object> generateClass(Class<?> declaringClass) {
        return new ByteBuddy()
                .subclass(Object.class)
                .innerTypeOf(declaringClass)
                .implement(implementationClass())
                .visit(new MyAsmVisitorWrapper());
    }

    protected Class<?> getLambdaRealClass(Method targetMethod) {
        Class<?> declaringClass = targetMethod.getDeclaringClass();
        if (declaringClass.getSimpleName().contains("$$Lambda/")) {
            Class<?> found = ReflectUtil.foreachClass(declaringClass, true, it -> {
                if (declaringClass == it) return null;

                try {
                    Method declaredMethod = it.getDeclaredMethod(targetMethod.getName(), targetMethod.getParameterTypes());
                    return it;
                } catch (NoSuchMethodException ignore) {

                }
                return null;
            });
            return found == null ? declaringClass : found;
        }
        return declaringClass;
    }

    protected void getTargetMethod(Method targetMethod,
                                   Implementation.Target implementationTarget,
                                   MethodVisitor methodVisitor,
                                   Implementation.Context implementationContext) {
        Class<?> declaringClass = getLambdaRealClass(targetMethod);
        Class<?>[] parameterTypes = targetMethod.getParameterTypes();

        ClassConstant.of(TypeDescription.ForLoadedType.of(declaringClass)).apply(methodVisitor, implementationContext);
        new TextConstant(targetMethod.getName()).apply(methodVisitor, implementationContext);
        generateClassArray(methodVisitor, implementationContext, parameterTypes);

        methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                false
        );
    }

    protected void generateClassArray(MethodVisitor methodVisitor, Implementation.Context implementationContext, Class<?>[] classes) {
        methodVisitor.visitLdcInsn(classes.length);
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
        for (int i = 0; i < classes.length; i++) {
            methodVisitor.visitInsn(Opcodes.DUP);
            methodVisitor.visitLdcInsn(i);
            ClassConstant.of(TypeDescription.ForLoadedType.of(classes[i])).apply(methodVisitor, implementationContext);
            methodVisitor.visitInsn(Opcodes.AASTORE);
        }
    }

    protected void getAndSetMethodHandle(Class<?> paramsType,
                                         Implementation.Target implementationTarget,
                                         MethodVisitor methodVisitor,
                                         Implementation.Context implementationContext) throws NoSuchMethodException {
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Lookup.class.getName().replace('.', '/'),
                "getMethodHandle",
                Type.getMethodDescriptor(Lookup.class.getDeclaredMethod("getMethodHandle", paramsType)),
                false
        );

        FieldDescription.InDefinedShape handle = implementationTarget.getInstrumentedType()
                .getDeclaredFields()
                .filter(named("handle"))
                .getOnly();

        methodVisitor.visitFieldInsn(
                Opcodes.PUTSTATIC,
                handle.getDeclaringType().getInternalName(),
                handle.getName(),
                handle.getDescriptor()
        );
    }

    protected ByteCodeAppender.Size generateInvoke(Executable executable,
                                                   Implementation.Target implementationTarget,
                                                   MethodVisitor methodVisitor,
                                                   Implementation.Context implementationContext,
                                                   MethodDescription methodDescription) {
        Class<?> declaringClass = executable.getDeclaringClass();
        if (executable instanceof Method) {
            declaringClass = getLambdaRealClass((Method) executable);
        }
        Class<?>[] parameterTypes = executable.getParameterTypes();
        boolean isStatic = Modifier.isStatic(executable.getModifiers());
        Class<?> returnType = executable instanceof Method ? ((Method) executable).getReturnType() : null;

        FieldDescription.InDefinedShape handle = implementationTarget.getInstrumentedType()
                .getDeclaredFields()
                .filter(named("handle"))
                .getOnly();
        methodVisitor.visitFieldInsn(
                Opcodes.GETSTATIC,
                handle.getDeclaringType().getInternalName(),
                handle.getName(),
                handle.getDescriptor());

        if (!isStatic && returnType != null) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, declaringClass.getName().replace(".", "/"));
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> needType = parameterTypes[i];
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitLdcInsn(i);
            methodVisitor.visitInsn(Opcodes.AALOAD);

            if (needType.isPrimitive()) {
                generateToUnboxType(methodVisitor, TypeDescription.ForLoadedType.of(needType));
            } else {
                methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, needType.getName().replace('.', '/'));
            }
        }
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/invoke/MethodHandle",
                "invokeExact",
                generateInvokeDescription(executable),
                false);
        if (returnType != null && returnType.isPrimitive()) {
            if (returnType == void.class) {
                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
            } else {
                generateBoxedTypeFrom(methodVisitor, returnType);
            }
        }
        // methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, targetMethod.getReturnType().getName().replace('.', '/'));
        methodVisitor.visitInsn(Opcodes.ARETURN);
        return new ByteCodeAppender.Size(parameterTypes.length * 4 + 3, methodDescription.getStackSize());
    }

    protected String generateInvokeDescription(Executable executable) {
        if (executable instanceof Method) {
            return generateInvokeDescription((Method) executable);
        } else {
            return generateInvokeDescription((Constructor<?>) executable);
        }
    }

    protected String generateInvokeDescription(Constructor<?> targetMethod) {
        StringBuilder sb = new StringBuilder("(");
        Class<?>[] parameterTypes = targetMethod.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            sb.append(TypeDescription.ForLoadedType.of(parameterType).getDescriptor());
        }
        sb.append(')').append(TypeDescription.ForLoadedType.of(targetMethod.getDeclaringClass()).getDescriptor());
        return sb.toString();
    }

    protected String generateInvokeDescription(Method targetMethod) {
        String methodDescriptor = Type.getMethodDescriptor(targetMethod);
        if (Modifier.isStatic(targetMethod.getModifiers())) {
            return methodDescriptor;
        }
        return "(L" + getLambdaRealClass(targetMethod).getName().replace('.', '/') + ";" + methodDescriptor.substring(1);
    }

    protected String generateGetterInvokeDescription(Field targetField) {
        TypeDescription declaringClass = TypeDescription.ForLoadedType.of(targetField.getDeclaringClass());
        TypeDescription targetType = TypeDescription.ForLoadedType.of(targetField.getType());
        if (Modifier.isStatic(targetField.getModifiers())) {
            return "()" + targetType.getDescriptor();
        } else {
            return "(" + declaringClass.getDescriptor() + ")" + targetType.getDescriptor();
        }
    }

    protected String generateSetterInvokeDescription(Field targetField) {
        TypeDescription declaringClass = TypeDescription.ForLoadedType.of(targetField.getDeclaringClass());
        TypeDescription targetType = TypeDescription.ForLoadedType.of(targetField.getType());
        if (Modifier.isStatic(targetField.getModifiers())) {
            return "(" + targetType.getDescriptor() + ")V";
        } else {
            return "(" + declaringClass.getDescriptor() + targetType.getDescriptor() + ")V";
        }
    }

    protected void generateBoxedTypeFrom(MethodVisitor methodVisitor, Class<?> type) {
        generateToBoxedType(methodVisitor, TypeDescription.ForLoadedType.of(type).asBoxed());
    }

    protected void generateToBoxedType(MethodVisitor methodVisitor, TypeDescription boxed) {
        TypeDescription unboxed = boxed.asUnboxed();

        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                boxed.getName().replace('.', '/'),
                "valueOf",
                "(" + unboxed.getDescriptor() + ")" + boxed.getDescriptor(),
                false
        );
    }

    protected void generateToUnboxType(MethodVisitor methodVisitor, TypeDescription unboxed) {
        TypeDescription boxed = unboxed.asBoxed();
        String name = boxed.getName().replace('.', '/');
        switch (boxed.getName()) {
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Float":
            case "java.lang.Double":
                name = Number.class.getName().replace('.', '/');
                break;
        }
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, name);

        methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                name,
                unboxed.getName() + "Value",
                "()" + unboxed.getDescriptor(),
                false
        );
    }
}
