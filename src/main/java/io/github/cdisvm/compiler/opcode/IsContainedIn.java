package io.github.cdisvm.compiler.opcode;

/**
 * Pops the two top items off the stack and checks if the second item is contained by the first.
 * <p>
 * If {@code negate} is true, the result is negated.
 * <p>
 * Stack Effect: -1
 * Prior: ..., item, collection
 * After: ..., is_contained
 *
 * <pre>{@code
 * >>> a in b
 * LoadLocal(name="a")
 * LoadLocal(name="b")
 * IsContainedIn(negate=False)
 *
 * >>> a not in b
 * LoadLocal(name="a")
 * LoadLocal(name="b")
 * IsContainedIn(negate=True)
 * }</pre>
 *
 * @param negate whether to negate the result (for "not in" vs "in")
 */
public record IsContainedIn(boolean negate) implements Opcode {
}
