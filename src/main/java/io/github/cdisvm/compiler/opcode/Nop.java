package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Does nothing.
 * <p>
 * Used to implement pass statements.
 * <p>
 * Stack Effect: 0
 * Prior: ...
 * After: ...
 *
 * <pre>{@code
 * >>> pass
 * Nop()
 * }</pre>
 */
public record Nop() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.nop();
    }
}
