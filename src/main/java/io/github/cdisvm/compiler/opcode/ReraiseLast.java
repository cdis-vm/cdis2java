package io.github.cdisvm.compiler.opcode;

/**
 * Re-raises the last exception raised.
 * <p>
 * Stack Effect: N/A
 * Prior: ...
 * After: N/A
 *
 * <pre>{@code
 * >>> try:
 * ...     raise TypeError
 * ... except:
 * ...     raise
 * LoadGlobal(name="TypeError")
 * Raise()
 * label handler
 * ReraiseLast()
 * }</pre>
 */
public record ReraiseLast() implements Opcode {
}
