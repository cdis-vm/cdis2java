package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Jumps to target unconditionally.
 * <p>
 * Stack Effect: 0
 * Prior: ...
 * After: ...
 *
 * <pre>{@code
 * >>> not x
 * LoadLocal(name="x")
 * IfTrue(target=is_true)
 * LoadConstant(constant=True)
 * JumpTo(target=done)
 * label is_true
 * LoadConstant(constant=False)
 * label done
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump to
 */
public record JumpTo(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.goto_(compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex));
    }
}
