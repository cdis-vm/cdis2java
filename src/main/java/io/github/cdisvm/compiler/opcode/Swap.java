package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Swaps the two top items on the stack.
 * <p>
 * Stack Effect: 0
 * Prior: ..., second, first
 * After: ..., first, second
 *
 * <pre>{@code
 * >>> x < y < z
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * DupX1()
 * BinaryOp(operator=BinaryOperator.Lt)
 * Dup()
 * IfFalse(target=exit_early)
 * Pop()
 * LoadLocal(name="z")
 * BinaryOp(operator=BinaryOperator.Lt)
 * JumpTo(target=done)
 * label exit_early
 * Swap()
 * Pop()
 * label done
 * }</pre>
 */
public record Swap() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.swap();
    }
}
