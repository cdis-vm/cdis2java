package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.Map;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Pop top of stack and merge it into the dict before it in the stack.
 * <p>
 * The dict remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., dict, mapping
 * After: ..., dict
 *
 * <pre>{@code
 * >>> {**items}
 * NewDict()
 * LoadLocal(name="items")
 * DictUpdate()
 * }</pre>
 */
public record DictUpdate() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.swap();
        codeBuilder.dup_x1();
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.PY_DICT, "putAll", MD.of(void.class, Map.class));
    }
}
