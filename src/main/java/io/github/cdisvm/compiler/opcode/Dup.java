package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Duplicates the value on top of stack.
 * <p>
 * Stack Effect: +1
 * Prior: ..., value
 * After: ..., value, value
 *
 * <pre>{@code
 * >>> x = y = 10
 * LoadConstant(constant=10)
 * Dup()
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * }</pre>
 */
public record Dup() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.dup();
    }
}
