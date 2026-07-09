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
 * Pops off the top of stack and gets its iterator.
 * <p>
 * Stack Effect: -1
 * Prior: ..., iterable
 * After: ..., iterator
 *
 * <pre>{@code
 * >>> for item in items:
 * ...     pass
 * LoadLocal(name="items")
 * GetIterator()
 * StoreSynthetic(index=0)
 * }</pre>
 */
public record GetIterator() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // TODO: Optimize if we know the type from stack metadata
        codeBuilder.invokestatic(CD.PY_ITERABLE, "wrapping", MD.of(PyIterable.class, PyObject.class), true);
        codeBuilder.invokeinterface(CD.PY_ITERABLE, "pyIterator", MD.of(PyIterator.class));
    }
}
