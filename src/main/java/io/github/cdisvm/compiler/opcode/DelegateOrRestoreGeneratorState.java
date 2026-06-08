package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.StackMetadata;

/**
 * Pops generator from TOS, restores the frame from the generator, then replaces TOS with the
 * sent value stored on the generator (or raises an exception if throw was called on the generator).
 * <p>
 * Stack Effect: 0
 * Prior: ..., generator
 * After: ..., sent_value_or_yield_from_return
 *
 * <pre>{@code
 * >>> yield 10
 * LoadConstant(constant=10)
 * LoadSynthetic(index=0)
 * SaveGeneratorState(stack=1, variables=(), closure=(), synthetic_variables=1)
 * YieldValue()
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(stack=1, variables=(), closure=(), synthetic_variables=1)
 * Pop()
 * }</pre>
 *
 * @param stateId the state identifier
 * @param savedStackMetadata the saved stack metadata for frame restoration
 */
public record DelegateOrRestoreGeneratorState(int stateId,
                                              StackMetadata savedStackMetadata) implements Opcode {
}
