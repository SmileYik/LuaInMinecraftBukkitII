package org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.pool.TypePool;

public class MyAsmVisitorWrapper implements AsmVisitorWrapper {

    /**
     * Defines the flags that are provided to any {@code ClassWriter} when writing a class. Typically, this gives opportunity to instruct ASM
     * to compute stack map frames or the size of the local variables array and the operand stack. If no specific flags are required for
     * applying this wrapper, the given value is to be returned.
     *
     * @param flags The currently set flags. This value should be combined (e.g. {@code flags | foo}) into the value that is returned by this wrapper.
     * @return The flags to be provided to the ASM {@code ClassWriter}.
     */
    @Override
    public int mergeWriter(int flags) {
        return flags | ClassWriter.COMPUTE_FRAMES;
    }

    /**
     * Defines the flags that are provided to any {@code ClassReader} when reading a class if applicable. Typically, this gives opportunity to
     * instruct ASM to expand or skip frames and to skip code and debug information. If no specific flags are required for applying this
     * wrapper, the given value is to be returned.
     *
     * @param flags The currently set flags. This value should be combined (e.g. {@code flags | foo}) into the value that is returned by this wrapper.
     * @return The flags to be provided to the ASM {@code ClassReader}.
     */
    @Override
    public int mergeReader(int flags) {
        return flags | ClassWriter.COMPUTE_FRAMES;
    }

    /**
     * Applies a {@code ClassVisitorWrapper} to the creation of a {@link DynamicType}.
     *
     * @param instrumentedType      The instrumented type.
     * @param classVisitor          A {@code ClassVisitor} to become the new primary class visitor to which the created
     *                              {@link DynamicType} is written to.
     * @param implementationContext The implementation context of the current instrumentation.
     * @param typePool              The type pool that was provided for the class creation.
     * @param fields                The instrumented type's fields.
     * @param methods               The instrumented type's methods non-ignored declared and virtually inherited methods.
     * @param writerFlags           The ASM {@link ClassWriter} flags to consider.
     * @param readerFlags           The ASM {@link ClassReader} flags to consider.
     * @return A new {@code ClassVisitor} that usually delegates to the {@code ClassVisitor} delivered in the argument.
     */
    @Override
    public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
        return classVisitor;
    }
}
