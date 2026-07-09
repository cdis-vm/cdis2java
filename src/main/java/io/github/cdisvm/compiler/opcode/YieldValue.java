package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Returns the value on top of stack and "pauses" execution.
 * <p>
 * Acts identically to ReturnValue.
 * <p>
 * Stack Effect: -1
 * Prior: ..., return_value
 * After: ...
 *
 * <pre>{@code
 * >>> yield 10
 * LoadConstant(constant=10)
 * LoadSynthetic(index=0)
 * SaveGeneratorState()
 * YieldValue()
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState()
 * Pop()
 * }</pre>
 */
public record YieldValue() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.return_(TypeKind.REFERENCE);
    }
}
