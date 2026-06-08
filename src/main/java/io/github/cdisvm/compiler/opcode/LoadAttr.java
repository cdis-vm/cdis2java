package io.github.cdisvm.compiler.opcode;

/**
 * Replaces top of stack with the result of an attribute lookup.
 * <p>
 * Attribute lookup calls {@code __getattribute__} on the type of the object on top of stack.
 * {@code __getattribute__} is relatively complex, handling descriptors, method resolution order
 * and class variables.
 * <p>
 * If {@code __getattribute__} raises {@code AttributeError}, it calls {@code __getattr__} if the
 * type has it defined, otherwise it raises the {@code AttributeError}.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., attribute
 *
 * <pre>{@code
 * >>> obj.attribute
 * LoadLocal(name="obj")
 * LoadAttr(name="attribute")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record LoadAttr(String attributeName) implements Opcode {
}
