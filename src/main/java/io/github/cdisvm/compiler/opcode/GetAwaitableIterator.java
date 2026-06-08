package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top of stack and gets its awaitable iterator.
 * <p>
 * Stack Effect: -1
 * Prior: ..., awaitable
 * After: ..., iterator
 *
 * <pre>{@code
 * >>> await task
 * LoadLocal(name="task")
 * GetAwaitableIterator()
 * LoadSynthetic(index=0)
 * SetGeneratorDelegate()
 * LoadSynthetic(index=0)
 * SaveGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * Pop()
 * }</pre>
 */
public record GetAwaitableIterator() implements Opcode {
}
