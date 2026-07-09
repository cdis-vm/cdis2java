package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyBool;

/**
 * Pops the two top items off the stack and checks if the second item is contained by the first.
 * <p>
 * If {@code negate} is true, the result is negated.
 * <p>
 * Stack Effect: -1
 * Prior: ..., item, collection
 * After: ..., is_contained
 *
 * <pre>{@code
 * >>> a in b
 * LoadLocal(name="a")
 * LoadLocal(name="b")
 * IsContainedIn(negate=False)
 *
 * >>> a not in b
 * LoadLocal(name="a")
 * LoadLocal(name="b")
 * IsContainedIn(negate=True)
 * }</pre>
 *
 * @param negate whether to negate the result (for "not in" vs "in")
 */
public record IsContainedIn(boolean negate) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokestatic(CD.of(PyContainer.class), "wrapping", MD.of(PyContainer.class, PyObject.class), true);
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PyContainer.class), "pyHasItem", MD.of(PyBool.class, PyObject.class));
        if (negate) {
            codeBuilder.invokevirtual(CD.PY_BOOL, "negate", MD.of(PyBool.class));
        }
    }
}
