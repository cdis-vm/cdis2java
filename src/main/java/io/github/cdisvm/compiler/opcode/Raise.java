package io.github.cdisvm.compiler.opcode;

/**
 * Raises the exception or exception type on the top of the stack.
 * <p>
 * Stack Effect: N/A
 * Prior: ..., exception
 * After: N/A
 *
 * <pre>{@code
 * >>> raise TypeError
 * LoadGlobal(name="TypeError")
 * Raise()
 * }</pre>
 */
public record Raise() implements Opcode {
}
