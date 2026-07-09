package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyIterator;
import io.github.cdisvm.runtime.PyObject;

/**
 * Gets the next element of the iterator at top of stack.
 * <p>
 * If next raises {@code StopIteration}, jump to target instead.
 * <p>
 * Stack Effect: 0 if iterator has next element, -1 otherwise
 * Prior: ..., iterator
 * After (has next element): ..., next_element
 * After (iterator exhausted): ...
 *
 * <pre>{@code
 * >>> for item in collection:
 * ...     pass
 * LoadLocal(name="collection")
 * GetIterator()
 * StoreSynthetic(index=0)
 *
 * label loop_start
 *
 * LoadSynthetic(index=0)
 * GetNextElseJumpTo(target=loop_end)
 * StoreLocal(name="item")
 * JumpTo(target=loop_start)
 *
 * label loop_end
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump to if the iterator is exhausted
 */
public record GetNextElseJumpTo(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var loopEnteranceLabel = codeBuilder.newLabel();
        codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
        codeBuilder.dup();
        codeBuilder.aconst_null();
        codeBuilder.if_acmpne(loopEnteranceLabel);
        codeBuilder.pop();
        codeBuilder.goto_(compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex));
        codeBuilder.labelBinding(loopEnteranceLabel);
    }
}
