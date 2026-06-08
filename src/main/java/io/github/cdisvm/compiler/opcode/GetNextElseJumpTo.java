package io.github.cdisvm.compiler.opcode;

/**
 * Gets the next element of the iterator at top of stack.
 * <p>
 * If next raises {@code StopIteration}, jump to target instead.
 * <p>
 * Stack Effect: 0 if iterator has next element, -1 otherwise
 * Prior: ..., iterator
 * After (has next element): ..., next_element
 * After (iterator exhausted): ...
 *
 * <pre>{@code
 * >>> for item in collection:
 * ...     pass
 * LoadLocal(name="collection")
 * GetIterator()
 * StoreSynthetic(index=0)
 *
 * label loop_start
 *
 * LoadSynthetic(index=0)
 * GetNextElseJumpTo(target=loop_end)
 * StoreLocal(name="item")
 * JumpTo(target=loop_start)
 *
 * label loop_end
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump to if the iterator is exhausted
 */
public record GetNextElseJumpTo(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
