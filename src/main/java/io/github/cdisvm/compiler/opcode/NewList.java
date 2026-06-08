package io.github.cdisvm.compiler.opcode;

/**
 * Push a new list into the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., new_list
 *
 * <pre>{@code
 * >>> []
 * NewList()
 * }</pre>
 */
public record NewList() implements Opcode {
}
