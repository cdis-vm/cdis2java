package io.github.cdisvm.compiler.opcode;

/**
 * Pop top of stack and merge it into the set before it in the stack.
 * <p>
 * The set remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., set, iterable
 * After: ..., set
 *
 * <pre>{@code
 * >>> {*items}
 * NewSet()
 * LoadLocal(name="items")
 * SetUpdate()
 * }</pre>
 */
public record SetUpdate() implements Opcode {
}
