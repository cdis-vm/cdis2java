package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.ClassInfo;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Loads and binds an inner generator.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., bound_inner_generator
 *
 * @param innerGenerator the inner generator class info
 */
public record LoadAndBindInnerGenerator(ClassInfo innerGenerator) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var generatorType = compilationRun.compiler().lookupUserType(innerGenerator());
        var generatorTypeClassDesc = CD.of(generatorType.getClass());
        codeBuilder.getstatic(generatorTypeClassDesc, "INSTANCE", generatorTypeClassDesc);
    }
}
