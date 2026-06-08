package io.github.cdisvm.compiler.opcode;

/**
 * TOS is generator, and the item below it is the delegate.
 * <p>
 * Stack Effect: -2
 * Prior: ..., iterable, generator
 * After: ...
 *
 * <pre>{@code
 * >>> yield from [1, 2, 3]
 * NewList()
 * LoadConstant(constant=1)
 * ListAppend()
 * LoadConstant(constant=2)
 * ListAppend()
 * LoadConstant(constant=3)
 * ListAppend()
 * GetIter()
 * LoadSynthetic(index=0)
 * SetGeneratorDelegate()
 * LoadSynthetic(index=0)
 * SaveGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * Pop()
 * }</pre>
 */
public record SetGeneratorDelegate() implements Opcode {
}
