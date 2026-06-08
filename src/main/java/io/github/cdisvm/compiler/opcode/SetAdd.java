package io.github.cdisvm.compiler.opcode;

/**
 * Pop top of stack and adds it to the set before it in the stack.
 * <p>
 * The set remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., set, item
 * After: ..., set
 *
 * <pre>{@code
 * >>> {0}
 * NewSet()
 * LoadConstant(constant=0)
 * SetAdd()
 * }</pre>
 */
public record SetAdd() implements Opcode {
}
