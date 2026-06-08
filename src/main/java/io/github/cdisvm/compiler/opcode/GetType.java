package io.github.cdisvm.compiler.opcode;

/**
 * Replaces top of stack with its type.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., type
 *
 * <pre>{@code
 * >>> type(obj)
 * LoadLocal(name="obj")
 * GetType()
 * }</pre>
 */
public record GetType() implements Opcode {
}
