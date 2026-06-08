package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Pops off the value on top of stack.
 * <p>
 * Used to pop off unused values, such as in expression statements.
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> x
 * LoadLocal(name="x")
 * Pop()
 * }</pre>
 */
public record Pop() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.pop();
    }
}
