package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PySlice;

/**
 * The top item on the stack is the step, the item below it the stop, and the item below that
 * the start.
 * <p>
 * Build a new slice from the start, end, and step. Equivalent to {@code slice(start, stop, step)}.
 * <p>
 * Stack Effect: -3
 * Prior: ..., start, end, step
 * After: ..., slice
 *
 * <pre>{@code
 * >>> items[1:3]
 * LoadLocal(name="items")
 * LoadConstant(constant=1)
 * LoadConstant(constant=3)
 * LoadConstant(constant=None)
 * BuildSlice()
 * GetItem()
 * }</pre>
 */
public record BuildSlice() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokestatic(CD.of(PySlice.class), "of", MD.of(PySlice.class, PyObject.class, PyObject.class, PyObject.class));
    }
}
