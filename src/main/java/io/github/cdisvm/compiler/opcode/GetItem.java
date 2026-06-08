package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top two items on the stack to get an item.
 * <p>
 * The top of stack is the index, and the item before it is the collection.
 * <p>
 * Stack Effect: -1
 * Prior: ..., collection, index
 * After: ..., item
 *
 * <pre>{@code
 * >>> items[0]
 * LoadLocal(name="items")
 * LoadConstant(constant=0)
 * GetItem()
 * }</pre>
 */
public record GetItem() implements Opcode {
}
