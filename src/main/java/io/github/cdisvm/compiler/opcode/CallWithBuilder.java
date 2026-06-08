package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pops top of stack and calls it.
 * <p>
 * Top of stack is a call builder object that was mutated in prior opcodes to contain the
 * callable and its arguments.
 * <p>
 * Stack Effect: 0
 * Prior: ..., call_builder
 * After: ..., result
 *
 * <pre>{@code
 * >>> func()
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * CallWithBuilder()
 *
 * >>> func(1)
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * LoadConstant(constant=1)
 * WithPositionalArg(index=0)
 * CallWithBuilder()
 * }</pre>
 */
public record CallWithBuilder() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                "pyCall",
                MD.of(PyObject.class));
    }
}
