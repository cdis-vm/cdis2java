package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pops top of stack and inserts it as the given positional argument.
 * <p>
 * Stack Effect: -1
 * Prior: ..., call_builder, positional_arg
 * After: ..., call_builder
 *
 * <pre>{@code
 * >>> func(1)
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * LoadConstant(constant=1)
 * WithPositionalArg(index=0)
 * CallWithBuilder()
 * }</pre>
 *
 * @param argumentIndex the index of the positional argument
 */
public record WithPositionalArg(int argumentIndex) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        if (argumentIndex < PyCallBuilder.MAX_POSITIONAL_ARG_METHOD) {
            codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                    "$" + argumentIndex,
                    MD.of(PyCallBuilder.class, PyObject.class));
        } else {
            new AppendPositionalArg().implement(codeBuilder, compilationRun, stackMetadata);
        }
    }
}
