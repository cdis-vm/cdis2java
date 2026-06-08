package io.github.cdisvm.compiler.opcode;

/**
 * Replaces top of stack with the result of an attribute lookup from its type.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., type_attribute
 *
 * <pre>{@code
 * >>> with ctx:
 * LoadLocal(name="ctx")
 * LoadObjectTypeAttr(name="__enter__")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record LoadObjectTypeAttr(String attributeName) implements Opcode {
}
