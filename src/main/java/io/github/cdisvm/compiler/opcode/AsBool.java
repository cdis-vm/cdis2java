package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.builtin.PyBool;

/**
 * Replaces top of stack with its truthfulness.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., bool
 *
 * <pre>{@code
 * >>> bool(obj)
 * LoadLocal(name="obj")
 * AsBool()
 * }</pre>
 */
public record AsBool() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyTruth", MD.of(PyBool.class));
    }
}
