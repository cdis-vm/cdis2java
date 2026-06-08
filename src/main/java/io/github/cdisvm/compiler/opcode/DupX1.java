package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Duplicates the value on top of stack behind the value before it.
 * <p>
 * Used for chained comparisons (i.e. x &lt; y &lt; z).
 * <p>
 * Stack Effect: +1
 * Prior: ..., second, first
 * After: ..., first, second, first
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
public record DupX1() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.dup_x1();
    }
}
