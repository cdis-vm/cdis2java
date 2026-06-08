package io.github.cdisvm.compiler.opcode;

/**
 * Pop top of stack and append it to the list before it in the stack.
 * <p>
 * The list remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., list, item
 * After: ..., list
 *
 * <pre>{@code
 * >>> [0]
 * NewList()
 * LoadConstant(constant=0)
 * ListAppend()
 * }</pre>
 */
public record ListAppend() implements Opcode {
}
