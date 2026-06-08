package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;

/**
 * Creates a call builder for the item on the top of stack.
 * <p>
 * Stack Effect: 0
 * Prior: ..., callable
 * After: ..., call_builder
 *
 * <pre>{@code
 * >>> func()
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * CallWithBuilder()
 * }</pre>
 */
public record CreateCallBuilder() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.checkcast(CD.PY_CALLABLE);
        codeBuilder.invokeinterface(CD.PY_CALLABLE,
                "pyCallBuilder",
                MD.of(PyCallBuilder.class)
        );
    }
}
