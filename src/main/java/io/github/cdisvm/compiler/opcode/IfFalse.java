package io.github.cdisvm.compiler.opcode;

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
}
