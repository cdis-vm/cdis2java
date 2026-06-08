package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the two top items on the stack and checks if they are the same reference.
 * <p>
 * If they are the same reference, {@code true} is pushed to the stack; otherwise {@code false}
 * is pushed to the stack. If {@code negate} is set, then the result is negated before being
 * pushed to the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., left, right
 * After: ..., result
 *
 * <pre>{@code
 * >>> x is y
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * IsSameAs(negate=False)
 *
 * >>> x is not y
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * IsSameAs(negate=True)
 * }</pre>
 *
 * @param negate whether to negate the result (for "is not" vs "is")
 */
public record IsSameAs(boolean negate) implements Opcode {
}
