package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyType;

/**
 * Replaces top of stack with its type.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., type
 *
 * <pre>{@code
 * >>> type(obj)
 * LoadLocal(name="obj")
 * GetType()
 * }</pre>
 */
public record GetType() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyType", MD.of(PyType.class));
    }
}
