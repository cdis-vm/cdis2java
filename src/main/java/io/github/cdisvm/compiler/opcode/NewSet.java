package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Push a new set into the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., new_set
 *
 * <pre>{@code
 * >>> {0}
 * NewSet()
 * LoadConstant(constant=0)
 * SetAdd()
 * }</pre>
 */
public record NewSet() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.new_(CD.PY_SET);
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.PY_SET, "<init>", MD.of(void.class));
    }
}
