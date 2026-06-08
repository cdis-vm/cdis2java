package io.github.cdisvm.compiler.opcode;

/**
 * Pop top of stack and merge it into the dict before it in the stack.
 * <p>
 * The dict remains on the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., dict, mapping
 * After: ..., dict
 *
 * <pre>{@code
 * >>> {**items}
 * NewDict()
 * LoadLocal(name="items")
 * DictUpdate()
 * }</pre>
 */
public record DictUpdate() implements Opcode {
}
