package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;

/**
 * Pops off the top two items on the stack to get an item.
 * <p>
 * The top of stack is the index, and the item before it is the collection.
 * <p>
 * Stack Effect: -1
 * Prior: ..., collection, index
 * After: ..., item
 *
 * <pre>{@code
 * >>> items[0]
 * LoadLocal(name="items")
 * LoadConstant(constant=0)
 * GetItem()
 * }</pre>
 */
public record GetItem() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // TODO: check from the bytecode if we know it a PyGettable
        codeBuilder.swap();
        codeBuilder.invokestatic(CD.of(PyGettable.class), "wrapping", MD.of(PyGettable.class, PyObject.class), true);
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PyGettable.class), "pyGetItem", MD.of(PyObject.class, PyObject.class));
    }
}
