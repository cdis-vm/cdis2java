package io.github.cdisvm.compiler.opcode;

/**
 * Returns the value on top of stack and "pauses" execution.
 * <p>
 * Acts identically to ReturnValue.
 * <p>
 * Stack Effect: -1
 * Prior: ..., return_value
 * After: ...
 *
 * <pre>{@code
 * >>> yield 10
 * LoadConstant(constant=10)
 * LoadSynthetic(index=0)
 * SaveGeneratorState()
 * YieldValue()
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState()
 * Pop()
 * }</pre>
 */
public record YieldValue() implements Opcode {
}
