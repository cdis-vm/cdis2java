package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pop top of stack and merge it into the set before it in the stack.
 * <p>
 * The set remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., set, iterable
 * After: ..., set
 *
 * <pre>{@code
 * >>> {*items}
 * NewSet()
 * LoadLocal(name="items")
 * SetUpdate()
 * }</pre>
 */
public record SetUpdate() implements Opcode {

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // set, iterable
        // TODO: optimize this based on type knowledge from stackMetadata
        new GetIterator().implement(codeBuilder, compilationRun, stackMetadata);
        var startLabel = codeBuilder.newBoundLabel();
        var endLabel = codeBuilder.newLabel();

        // set, iterator
        codeBuilder.dup();
        codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
        // set, iterator, next
        codeBuilder.dup();
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(endLabel);
        // set, iterator, next
        codeBuilder.swap();
        codeBuilder.dup_x2();
        codeBuilder.pop();
        // iterator, set, next
        codeBuilder.swap();
        codeBuilder.dup_x2();
        codeBuilder.swap();
        // set, iterator, set, next
        codeBuilder.invokevirtual(CD.PY_SET,
                "add", MD.of(boolean.class, Object.class));
        codeBuilder.pop();
        // set, iterator
        codeBuilder.goto_(startLabel);
        codeBuilder.labelBinding(endLabel);
        // set, iterator, next
        codeBuilder.pop2();
        // set
    }
}
