package io.github.cdisvm.compiler.opcode;

/**
 * Raises the exception behind top of stack with top of stack as the cause.
 * <p>
 * Stack Effect: N/A
 * Prior: ..., exception, cause
 * After: N/A
 *
 * <pre>{@code
 * >>> raise TypeError from ValueError
 * LoadGlobal(name="TypeError")
 * LoadGlobal(name="ValueError")
 * RaiseWithCause()
 * }</pre>
 */
public record RaiseWithCause() implements Opcode {
}
