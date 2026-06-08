package io.github.cdisvm.compiler.opcode;

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
}
