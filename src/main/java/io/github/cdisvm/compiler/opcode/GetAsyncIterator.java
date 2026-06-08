package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top of stack and gets its async for iterator.
 * <p>
 * Stack Effect: -1
 * Prior: ..., async_iterable
 * After: ..., async_iterator
 *
 * <pre>{@code
 * >>> async for item in items:
 * ...     pass
 * LoadLocal(name="items")
 * GetAsyncIterator()
 * LoadSynthetic(index=0)
 * SetGeneratorDelegate()
 *
 * label loop_start
 * LoadSynthetic(index=0)
 * SaveGeneratorState(StackMetadata(stack=1, variables=('item', 'items'), closure=(), synthetic_variables=1))
 * LoadSynthetic(index=0)
 *
 * try:
 *     DelegateOrRestoreGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 *     StoreLocal(name="item")
 * except AsyncStopIteration:
 *     JumpTo(target=loop_end)
 *
 * Nop()
 * JumpTo(target=loop_start)
 * label loop_end
 * }</pre>
 */
public record GetAsyncIterator() implements Opcode {
}
