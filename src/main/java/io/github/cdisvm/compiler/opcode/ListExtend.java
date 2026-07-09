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
        codeBuilder.swap();
        codeBuilder.dup_x1();
        codeBuilder.swap();
        // TODO: optimize this based on type knowledge from stackMetadata
        new GetIterator().implement(codeBuilder, compilationRun, stackMetadata);
        var startLabel = codeBuilder.newBoundLabel();
        var endLabel = codeBuilder.newLabel();
        codeBuilder.dup();
        codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
        codeBuilder.dup();
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(endLabel);
        codeBuilder.invokevirtual(CD.PY_LIST,
             "add", MD.of(boolean.class, Object.class));
        codeBuilder.pop();
        codeBuilder.goto_(startLabel);
        codeBuilder.labelBinding(endLabel);
    }
}
