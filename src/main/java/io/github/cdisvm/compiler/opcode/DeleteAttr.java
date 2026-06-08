package io.github.cdisvm.compiler.opcode;

/**
 * Deletes an attribute of the object on top of stack.
 * <p>
 * This calls {@code __delattr__} on the type of the object.
 * <p>
 * Stack Effect: -1
 * Prior: ..., object
 * After: ...
 *
 * <pre>{@code
 * >>> del obj.attribute
 * LoadLocal(name="obj")
 * DeleteAttr(name="attribute")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record DeleteAttr(String attributeName) implements Opcode {
}
