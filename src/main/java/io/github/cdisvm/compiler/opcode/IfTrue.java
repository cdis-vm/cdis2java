package io.github.cdisvm.compiler.opcode;

/**
 * Pops top of stack and jumps to target if it is truthy.
 * <p>
 * Stack Effect: -1
 * Prior: ..., condition
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
 *
 * >>> a or b
 * LoadLocal(name="a")
 * Dup()
 * IfTrue(target=done)
 * Pop()
 * LoadLocal(name="b")
 * label done
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump if the condition is truthy
 */
public record IfTrue(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
