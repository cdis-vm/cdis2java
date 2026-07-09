package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;

/**
 * Pops off the top three items on the stack to set an item in the collection.
 * <p>
 * The top of stack is the index, the item before it is the collection, and the item before the
 * collection is the value the index is set to.
 * <p>
 * Stack Effect: -3
 * Prior: ..., value, collection, index
 * After: ...
 *
 * <pre>{@code
 * >>> items[0] = 10
 * LoadConstant(constant=10)
 * LoadLocal(name="items")
 * LoadConstant(constant=0)
 * SetItem()
 * }</pre>
 */
public record SetItem() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.swap();
        // TODO: check from stackMetadata if we know it is a PySettable object
        codeBuilder.invokestatic(CD.of(PySettable.class), "wrapping", MD.of(PySettable.class, PyObject.class), true);
        codeBuilder.dup_x2();
        codeBuilder.pop();
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PySettable.class), "pySetItem", MD.of(void.class, PyObject.class, PyObject.class));
    }
}
