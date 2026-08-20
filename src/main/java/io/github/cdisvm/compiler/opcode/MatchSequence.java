package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PySequence;
import io.github.cdisvm.runtime.PySizable;
import io.github.cdisvm.runtime.builtin.PyInt;
import io.github.cdisvm.runtime.builtin.PySequenceBase;

/**
 * Top of stack is the queried object.
 * <p>
 * Do not pop it off the stack, and check if it is a sequence with at least length elements
 * (exact if isExact is true). If it is not a sequence of at least the specified length, jump
 * to target.
 * <p>
 * Stack Effect: 0
 * Prior: ..., query
 * After: ..., query
 *
 * <pre>{@code
 * >>> match query:
 * ...     case [x, y]:
 * ...         pass
 * LoadLocal(name="query")
 * MatchSequence(length=2, is_exact=True, target=no_match)
 * UnpackElements(before_count=2, after_count=0, has_extras=False, target=no_match)
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * JumpTo(target=end_match)
 * label no_match
 * Pop()
 * label end_match
 * }</pre>
 *
 * @param length the minimum (or exact) sequence length required
 * @param isExact whether the length must be exact
 * @param targetBytecodeIndex where to jump if the sequence does not match
 */
public record MatchSequence(int length,
                            boolean isExact,
                            int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var notMatchLabel = compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex);
        codeBuilder.dup();
        codeBuilder.instanceOf(CD.of(PySequence.class));
        codeBuilder.ifeq(notMatchLabel);
        codeBuilder.dup();
        codeBuilder.checkcast(CD.of(PySizable.class));
        codeBuilder.invokeinterface(CD.of(PySizable.class), "pyLength", MD.of(PyInt.class));
        codeBuilder.invokevirtual(CD.of(PyInt.class), "intValue", MD.of(int.class));
        codeBuilder.loadConstant(length);
        if (isExact) {
            codeBuilder.if_icmpne(notMatchLabel);
        } else {
            codeBuilder.if_icmplt(notMatchLabel);
        }
    }
}
