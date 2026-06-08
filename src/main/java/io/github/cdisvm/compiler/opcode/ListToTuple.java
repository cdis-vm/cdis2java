package io.github.cdisvm.compiler.opcode;

/**
 * Unpacks the list at the top of the stack into a tuple and push that tuple to the stack.
 * <p>
 * Stack Effect: 0
 * Prior: ..., list
 * After: ..., tuple
 *
 * <pre>{@code
 * >>> 0, 1
 * NewList()
 * LoadConstant(constant=0)
 * ListAppend()
 * LoadConstant(constant=1)
 * ListAppend()
 * ListToTuple()
 * }</pre>
 */
public record ListToTuple() implements Opcode {
}
