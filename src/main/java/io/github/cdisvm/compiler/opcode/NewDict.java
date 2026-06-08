package io.github.cdisvm.compiler.opcode;

/**
 * Push a new dict into the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., new_dict
 *
 * <pre>{@code
 * >>> {}
 * NewDict()
 * }</pre>
 */
public record NewDict() implements Opcode {
}
