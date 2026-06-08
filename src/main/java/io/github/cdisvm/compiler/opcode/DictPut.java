package io.github.cdisvm.compiler.opcode;

/**
 * Pops the top two items off the stack and put them in the dict prior to them.
 * <p>
 * The dict remains on the stack. The top of stack is the value, and the item before it is the key.
 * <p>
 * Stack Effect: -2
 * Prior: ..., dict, key, value
 * After: ..., dict
 *
 * <pre>{@code
 * >>> {"key": "value"}
 * NewDict()
 * LoadConstant(constant="key")
 * LoadConstant(constant="value")
 * DictPut()
 * }</pre>
 */
public record DictPut() implements Opcode {
}
