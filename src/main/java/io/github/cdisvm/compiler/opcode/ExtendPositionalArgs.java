package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;

/**
 * Pops top of stack and unpacks it into the positional argument list.
 * <p>
 * Stack Effect: -1
 * Prior: ..., call_builder, iterable
 * After: ..., call_builder
 *
 * <pre>{@code
 * >>> func(*args)
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * LoadLocal(name="args")
 * ExtendPositionalArgs()
 * CallWithBuilder()
 * }</pre>
 */
public record ExtendPositionalArgs() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.checkcast(CD.of(Iterable.class));
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                "$extendArguments",
                MD.of(PyCallBuilder.class, Iterable.class));
    }
}
