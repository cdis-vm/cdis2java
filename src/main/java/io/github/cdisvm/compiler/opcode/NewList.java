package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.builtin.PyList;

/**
 * Push a new list into the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., new_list
 *
 * <pre>{@code
 * >>> []
 * NewList()
 * }</pre>
 */
public record NewList() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.new_(CD.PY_LIST);
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.PY_LIST, "<init>", MD.of(void.class));
    }
}
