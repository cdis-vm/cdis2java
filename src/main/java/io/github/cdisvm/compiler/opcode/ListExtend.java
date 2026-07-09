package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pop top of stack and use it to extend the list before it in the stack.
 * <p>
 * The list remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., list, iterable
 * After: ..., list
 *
 * <pre>{@code
 * >>> [*items]
 * NewList()
 * LoadLocal(name="items")
 * ListExtend()
 * }</pre>
 */
public record ListExtend() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // list, iterable
        // TODO: optimize this based on type knowledge from stackMetadata
        new GetIterator().implement(codeBuilder, compilationRun, stackMetadata);
        var startLabel = codeBuilder.newBoundLabel();
        var endLabel = codeBuilder.newLabel();

        // list, iterator
        codeBuilder.dup();
        codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
        // list, iterator, next
        codeBuilder.dup();
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(endLabel);
        // list, iterator, next
        codeBuilder.swap();
        codeBuilder.dup_x2();
        codeBuilder.pop();
        // iterator, list, next
        codeBuilder.swap();
        codeBuilder.dup_x2();
        codeBuilder.swap();
        // list, iterator, list, next
        codeBuilder.invokevirtual(CD.PY_LIST,
             "add", MD.of(boolean.class, Object.class));
        codeBuilder.pop();
        // list, iterator
        codeBuilder.goto_(startLabel);
        codeBuilder.labelBinding(endLabel);
        // list, iterator, next
        codeBuilder.pop2();
        // list
    }
}
