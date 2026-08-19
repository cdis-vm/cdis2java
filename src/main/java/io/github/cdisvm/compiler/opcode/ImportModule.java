package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.ClassInfo;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Push the module with the given name to the stack.
 * <p>
 * Unlike CPython/cdis, The module must already resolved and is represented by a {@link ClassInfo} object.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., module
 *
 * <pre>{@code
 * >>> import cdis
 * ImportModule(name='cdis', level=0, from_list=())
 * }</pre>
 *
 * @param moduleAsClass the resolved module
 */
@NullMarked
public record ImportModule(ClassInfo moduleAsClass) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        compilationRun.compiler().lookupUserType(moduleAsClass).loadValueOntoStack(codeBuilder);
    }
}
