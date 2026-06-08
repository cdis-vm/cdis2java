package io.github.cdisvm.compiler.opcode;

/**
 * Top of stack is the queried object.
 * <p>
 * Do not pop it off the stack, and check if it is a sequence with at least length elements
 * (exact if isExact is true). If it is not a sequence of at least the specified length, jump
 * to target.
 * <p>
 * Stack Effect: 0
 * Prior: ..., query
 * After: ..., query
 *
 * <pre>{@code
 * >>> match query:
 * ...     case [x, y]:
 * ...         pass
 * LoadLocal(name="query")
 * MatchSequence(length=2, is_exact=True, target=no_match)
 * UnpackElements(before_count=2, after_count=0, has_extras=False, target=no_match)
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * JumpTo(target=end_match)
 * label no_match
 * Pop()
 * label end_match
 * }</pre>
 *
 * @param length the minimum (or exact) sequence length required
 * @param isExact whether the length must be exact
 * @param targetBytecodeIndex where to jump if the sequence does not match
 */
public record MatchSequence(int length,
                            boolean isExact,
                            int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
