package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyConstant;

public record LoadConstant(PyConstant constant) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.getstatic(compilationRun.callableClassDesc(), constant.getJavaIdentifierName(), ClassDesc.of(constant.getClass().getCanonicalName()));
    }
}
