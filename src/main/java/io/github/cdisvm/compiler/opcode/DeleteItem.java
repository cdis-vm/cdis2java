package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top two items on the stack to delete an item.
 * <p>
 * The top of stack is the index, and the item before it is the collection.
 * <p>
 * Stack Effect: -2
 * Prior: ..., collection, index
 * After: ...
 *
 * <pre>{@code
 * >>> del items[0]
 * LoadLocal(name="items")
 * LoadConstant(constant=0)
 * DeleteItem()
 * }</pre>
 */
public record DeleteItem() implements Opcode {
}
