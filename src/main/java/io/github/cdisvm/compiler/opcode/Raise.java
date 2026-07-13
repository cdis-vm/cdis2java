package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

/**
 * Raises the exception or exception type on the top of the stack.
 * <p>
 * Stack Effect: N/A
 * Prior: ..., exception
 * After: N/A
 *
 * <pre>{@code
 * >>> raise TypeError
 * LoadGlobal(name="TypeError")
 * Raise()
 * }</pre>
 */
public record Raise() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.dup();
        codeBuilder.instanceOf(CD.of(PyType.class));
        var isExceptionInstanceLabel = codeBuilder.newLabel();
        codeBuilder.ifeq(isExceptionInstanceLabel);
        codeBuilder.checkcast(CD.of(PyType.class));
        codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER, "pyCall", MD.of(PyObject.class));

        codeBuilder.labelBinding(isExceptionInstanceLabel);
        codeBuilder.checkcast(CD.of(Throwable.class));
        codeBuilder.athrow();
    }
}
