package io.github.cdisvm.compiler.opcode;

/**
 * Top of stack is an exception type and the item below it is an exception.
 * <p>
 * If the exception is not an instance of the exception type, jump to target. If the exception
 * type is not a subclass of BaseException, raise {@code TypeError}.
 * <p>
 * Stack Effect: -2
 * Prior: ..., exception, exception_type
 * After: ...
 *
 * <pre>{@code
 * >>> try:
 * ...     pass
 * ... except ValueError:
 * ...     pass
 * StoreSynthetic(index=0)
 * LoadSynthetic(index=0)
 * LoadGlobal(name="ValueError")
 * JumpIfNotMatchExceptType(target=reraise)
 * Nop()
 * JumpTo(target=continue)
 *
 * label reraise
 * ReraiseLast()
 *
 * label continue
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump if the exception type does not match
 */
public record JumpIfNotMatchExceptType(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
