package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Returns the value on top of stack.
 * <p>
 * Stack Effect: N/A
 * Prior: ..., return_value
 * After: N/A
 *
 * <pre>{@code
 * >>> return 10
 * LoadConstant(constant=10)
 * ReturnValue()
 * }</pre>
 */
public record ReturnValue() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.return_(TypeKind.REFERENCE);
    }
}
