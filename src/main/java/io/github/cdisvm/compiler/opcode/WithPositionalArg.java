package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;

public record WithPositionalArg(int argumentIndex) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var interfaceCD = compilationRun.compiler().getFunctionParameterByIndexClassDesc(argumentIndex);
        codeBuilder.swap();
        codeBuilder.checkcast(interfaceCD);
        codeBuilder.swap();
        codeBuilder.invokeinterface(interfaceCD,
                "$" + argumentIndex,
                MD.of(PyCallBuilder.class, PyObject.class));
    }
}
