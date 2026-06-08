package io.github.cdisvm.compiler.opcode;

/**
 * Replaces top of stack with its truthfulness.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., bool
 *
 * <pre>{@code
 * >>> bool(obj)
 * LoadLocal(name="obj")
 * AsBool()
 * }</pre>
 */
public record AsBool() implements Opcode {
}
