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
 * Re-raises the last exception raised.
 * <p>
 * Stack Effect: N/A
 * Prior: ...
 * After: N/A
 *
 * <pre>{@code
 * >>> try:
 * ...     raise TypeError
 * ... except:
 * ...     raise
 * LoadGlobal(name="TypeError")
 * Raise()
 * label handler
 * ReraiseLast()
 * }</pre>
 */
public record ReraiseLast() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.aload(compilationRun.getLastRaisedExceptionSlot());
        codeBuilder.checkcast(CD.of(Throwable.class));
        codeBuilder.athrow();
    }
}
