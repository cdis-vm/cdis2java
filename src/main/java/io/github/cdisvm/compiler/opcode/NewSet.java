package io.github.cdisvm.compiler.opcode;

/**
 * Push a new set into the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., new_set
 *
 * <pre>{@code
 * >>> {0}
 * NewSet()
 * LoadConstant(constant=0)
 * SetAdd()
 * }</pre>
 */
public record NewSet() implements Opcode {
}
