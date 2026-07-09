package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Pop top of stack and adds it to the set before it in the stack.
 * <p>
 * The set remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., set, item
 * After: ..., set
 *
 * <pre>{@code
 * >>> {0}
 * NewSet()
 * LoadConstant(constant=0)
 * SetAdd()
 * }</pre>
 */
public record SetAdd() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.swap();
        codeBuilder.dup_x1();
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.PY_SET,
                "add", MD.of(boolean.class, Object.class));
        codeBuilder.pop();
    }
}
