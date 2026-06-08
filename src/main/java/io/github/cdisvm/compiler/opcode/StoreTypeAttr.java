package io.github.cdisvm.compiler.opcode;

/**
 * TOS is a mapping, and the item below it is a value.
 * <p>
 * Stores the value into the mapping.
 * <p>
 * Stack Effect: -2
 * Prior: ..., value, mapping
 * After: ...
 *
 * <pre>{@code
 * >>> class A:
 * ...     x = 0
 * LoadConstant(constant=0)
 * LoadSynthetic(index=0)
 * StoreTypeAttr(name="x")
 * }</pre>
 *
 * @param name the name of the type attribute
 */
public record StoreTypeAttr(String name) implements Opcode {
}
