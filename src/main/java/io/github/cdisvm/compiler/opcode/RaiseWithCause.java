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
 * Raises the exception behind top of stack with top of stack as the cause.
 * <p>
 * Stack Effect: N/A
 * Prior: ..., exception, cause
 * After: N/A
 *
 * <pre>{@code
 * >>> raise TypeError from ValueError
 * LoadGlobal(name="TypeError")
 * LoadGlobal(name="ValueError")
 * RaiseWithCause()
 * }</pre>
 */
public record RaiseWithCause() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.dup();
        codeBuilder.instanceOf(CD.of(PyType.class));
        var isCauseExceptionInstanceLabel = codeBuilder.newLabel();
        codeBuilder.ifeq(isCauseExceptionInstanceLabel);
        codeBuilder.checkcast(CD.of(PyType.class));
        codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER, "pyCall", MD.of(PyObject.class));

        codeBuilder.labelBinding(isCauseExceptionInstanceLabel);
        codeBuilder.checkcast(CD.of(Throwable.class));
        codeBuilder.swap();

        codeBuilder.dup();
        codeBuilder.instanceOf(CD.of(PyType.class));
        var isExceptionInstanceLabel = codeBuilder.newLabel();
        codeBuilder.ifeq(isExceptionInstanceLabel);
        codeBuilder.checkcast(CD.of(PyType.class));
        codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER, "pyCall", MD.of(PyObject.class));

        codeBuilder.labelBinding(isExceptionInstanceLabel);
        codeBuilder.checkcast(CD.of(Throwable.class));
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.of(Throwable.class), "initCause", MD.of(Throwable.class, Throwable.class));
        codeBuilder.athrow();
    }
}
