package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top three items on the stack to set an item in the collection.
 * <p>
 * The top of stack is the index, the item before it is the collection, and the item before the
 * collection is the value the index is set to.
 * <p>
 * Stack Effect: -3
 * Prior: ..., value, collection, index
 * After: ...
 *
 * <pre>{@code
 * >>> items[0] = 10
 * LoadConstant(constant=10)
 * LoadLocal(name="items")
 * LoadConstant(constant=0)
 * SetItem()
 * }</pre>
 */
public record SetItem() implements Opcode {
}
