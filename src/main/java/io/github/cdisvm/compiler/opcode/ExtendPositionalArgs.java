package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;

public record ExtendPositionalArgs() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.checkcast(CD.of(Iterable.class));
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                "$extendArguments",
                MD.of(PyCallBuilder.class, Iterable.class));
    }
}
