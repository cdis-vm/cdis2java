package io.github.cdisvm.compiler.opcode;

/**
 * Sets an attribute of an object.
 * <p>
 * Pop two items from the stack. The first (top of stack) is the object, and the second is the
 * value. This calls {@code __setattr__} on the type of the object with value.
 * <p>
 * Stack Effect: -2
 * Prior: ..., value, object
 * After: ...
 *
 * <pre>{@code
 * >>> obj.attribute = 10
 * LoadConstant(constant=10)
 * LoadLocal(name="obj")
 * StoreAttr(name="attribute")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record StoreAttr(String attributeName) implements Opcode {
}
