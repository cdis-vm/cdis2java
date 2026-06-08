package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top of stack and gets its iterator.
 * <p>
 * Stack Effect: -1
 * Prior: ..., iterable
 * After: ..., iterator
 *
 * <pre>{@code
 * >>> for item in items:
 * ...     pass
 * LoadLocal(name="items")
 * GetIterator()
 * StoreSynthetic(index=0)
 * }</pre>
 */
public record GetIterator() implements Opcode {
}
