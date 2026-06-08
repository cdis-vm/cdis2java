package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.StackMetadata;

/**
 * Saves the frame to the generator at TOS, then pops the generator.
 * <p>
 * Stack Effect: -1
 * Prior: ..., generator
 * After: ...
 *
 * <pre>{@code
 * >>> yield 10
 * LoadConstant(constant=10)
 * LoadSynthetic(index=0)
 * SaveGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * YieldValue()
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * Pop()
 * }</pre>
 *
 * @param stateId the state identifier
 * @param savedStackMetadata the state of the frame when this opcode is executed
 */
public record SaveGeneratorState(int stateId, StackMetadata savedStackMetadata) implements Opcode {
}
