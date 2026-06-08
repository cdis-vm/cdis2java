package io.github.cdisvm.compiler.opcode;

/**
 * TOS is a mapping.
 * <p>
 * Delete the given key from the mapping.
 * <p>
 * Stack Effect: -1
 * Prior: ..., mapping
 * After: ...
 *
 * <pre>{@code
 * >>> class A:
 * ...     x = 0
 * ...     del x
 * LoadConstant(constant=0)
 * LoadSynthetic(index=0)
 * StoreTypeAttr(name="x")
 * LoadSynthetic(index=0)
 * DeleteTypeAttr(name="x")
 * }</pre>
 *
 * @param name the name of the type attribute to delete
 */
public record DeleteTypeAttr(String name) {
}
