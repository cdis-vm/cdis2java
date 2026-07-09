package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.builtin.PyBool;

/**
 * Pops top of stack and jumps to target if it is falsey.
 * <p>
 * Stack Effect: -1
 * Prior: ..., condition
 * After: ...
 *
 * <pre>{@code
 * >>> a and b
 * LoadLocal(name="a")
 * Dup()
 * IfFalse(target=done)
 * Pop()
 * LoadLocal(name="b")
 * label done
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump if the condition is falsey
 */
public record IfFalse(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyTruth", MD.of(PyBool.class));
        codeBuilder.getstatic(CD.PY_BOOL, "FALSE", CD.PY_BOOL);
        codeBuilder.if_acmpeq(compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex));
    }
}
