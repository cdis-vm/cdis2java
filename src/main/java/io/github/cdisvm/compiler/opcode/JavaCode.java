package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.function.BiConsumer;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

public record JavaCode(BiConsumer<CodeBuilder, CompilationRun> codeBuilderConsumer) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilderConsumer.accept(codeBuilder, compilationRun);
    }
}
