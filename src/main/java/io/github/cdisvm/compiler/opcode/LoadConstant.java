package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyConstant;

/**
 * Loads a constant onto the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., constant
 *
 * <pre>{@code
 * >>> 1
 * LoadConstant(constant=1)
 *
 * >>> "hello"
 * LoadConstant(constant="hello")
 * }</pre>
 *
 * @param constant the constant to be loaded, for instance an int, float or str
 */
public record LoadConstant(PyConstant constant) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.getstatic(compilationRun.callableClassDesc(), constant.getJavaIdentifierName(), ClassDesc.of(constant.getClass().getCanonicalName()));
    }
}
