package io.github.cdisvm.compiler.opcode;

/**
 * Pop top of stack and use it to extend the list before it in the stack.
 * <p>
 * The list remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., list, iterable
 * After: ..., list
 *
 * <pre>{@code
 * >>> [*items]
 * NewList()
 * LoadLocal(name="items")
 * ListExtend()
 * }</pre>
 */
public record ListExtend() implements Opcode {
}
