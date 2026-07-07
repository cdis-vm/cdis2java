package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.util.List;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.builtin.PyTuple;

/**
 * Unpacks the list at the top of the stack into a tuple and push that tuple to the stack.
 * <p>
 * Stack Effect: 0
 * Prior: ..., list
 * After: ..., tuple
 *
 * <pre>{@code
 * >>> 0, 1
 * NewList()
 * LoadConstant(constant=0)
 * ListAppend()
 * LoadConstant(constant=1)
 * ListAppend()
 * ListToTuple()
 * }</pre>
 */
public record ListToTuple() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.new_(CD.PY_TUPLE);
        codeBuilder.dup_x1();
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.PY_LIST, "getDelegate", MD.of(List.class));
        codeBuilder.invokespecial(CD.PY_TUPLE, "<init>", MD.of(void.class, List.class));
    }
}
