package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.builtin.PyList;

/**
 * Pop top of stack and append it to the list before it in the stack.
 * <p>
 * The list remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., list, item
 * After: ..., list
 *
 * <pre>{@code
 * >>> [0]
 * NewList()
 * LoadConstant(constant=0)
 * ListAppend()
 * }</pre>
 */
public record ListAppend() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.swap();
        codeBuilder.dup_x1();
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.PY_LIST,
                "add", MD.of(boolean.class, Object.class));
        codeBuilder.pop();
    }
}
