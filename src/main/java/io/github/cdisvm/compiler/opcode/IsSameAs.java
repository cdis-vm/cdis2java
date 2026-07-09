package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Pops off the two top items on the stack and checks if they are the same reference.
 * <p>
 * If they are the same reference, {@code true} is pushed to the stack; otherwise {@code false}
 * is pushed to the stack. If {@code negate} is set, then the result is negated before being
 * pushed to the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., left, right
 * After: ..., result
 *
 * <pre>{@code
 * >>> x is y
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * IsSameAs(negate=False)
 *
 * >>> x is not y
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * IsSameAs(negate=True)
 * }</pre>
 *
 * @param negate whether to negate the result (for "is not" vs "is")
 */
public record IsSameAs(boolean negate) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var isNotEqualLabel = codeBuilder.newLabel();
        var endLabel = codeBuilder.newLabel();

        var whenSame = (negate)? "FALSE" : "TRUE";
        var whenNotSame = (negate)? "TRUE" : "FALSE";

        codeBuilder.if_acmpne(isNotEqualLabel);
        codeBuilder.getstatic(CD.PY_BOOL, whenSame, CD.PY_BOOL);
        codeBuilder.goto_(endLabel);
        codeBuilder.labelBinding(isNotEqualLabel);
        codeBuilder.getstatic(CD.PY_BOOL, whenNotSame, CD.PY_BOOL);
        codeBuilder.labelBinding(endLabel);
    }
}
