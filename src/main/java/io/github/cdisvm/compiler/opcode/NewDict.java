package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Push a new dict into the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., new_dict
 *
 * <pre>{@code
 * >>> {}
 * NewDict()
 * }</pre>
 */
public record NewDict() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.new_(CD.PY_DICT);
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.PY_DICT, "<init>", MD.of(void.class));
    }
}
