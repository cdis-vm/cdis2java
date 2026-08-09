package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyDeletable;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pops off the top two items on the stack to delete an item.
 * <p>
 * The top of stack is the index, and the item before it is the collection.
 * <p>
 * Stack Effect: -2
 * Prior: ..., collection, index
 * After: ...
 *
 * <pre>{@code
 * >>> del items[0]
 * LoadLocal(name="items")
 * LoadConstant(constant=0)
 * DeleteItem()
 * }</pre>
 */
public record DeleteItem() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // TODO: check from the bytecode if we know it a PyDeletable
        codeBuilder.swap();
        codeBuilder.invokestatic(CD.of(PyDeletable.class), "wrapping", MD.of(PyDeletable.class, PyObject.class), true);
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PyDeletable.class), "pyDeleteItem", MD.of(void.class, PyObject.class));
    }
}
