package io.github.cdisvm.compiler.opcode;

/**
 * Pops off the top of stack, and pushes its elements onto the stack in reversed order.
 * <p>
 * If {@code hasExtras} is false, {@code beforeCount} is the exact number of elements expected in
 * the iterable, and {@code afterCount} is 0.
 * <p>
 * If {@code hasExtras} is true, the first beforeCount elements of the iterable are added last to
 * the stack, then a list containing the items that are not first beforeCount elements of the
 * iterable or the last afterCount elements of the iterable is put between, and finally the last
 * afterCount elements of the iterable are put before the list.
 * <p>
 * Stack Effect: beforeCount + afterCount + (1 if hasExtras else 0) - 1
 * Prior: ..., iterable
 * After: ..., last_after_count, ..., last_after_1, extras_list, first_before_count, ..., first_1
 *
 * <pre>{@code
 * >>> a, b = 1, 2
 * NewList()
 * LoadConstant(constant=1)
 * ListAppend()
 * LoadConstant(constant=2)
 * ListAppend()
 * UnpackElements(before_count=2)
 * StoreLocal(name="a")  # 1
 * StoreLocal(name="b")  # 2
 *
 * >>> a, *b, c = 1, 2, 3, 4
 * NewList()
 * LoadConstant(constant=1)
 * ListAppend()
 * LoadConstant(constant=2)
 * ListAppend()
 * LoadConstant(constant=3)
 * ListAppend()
 * LoadConstant(constant=4)
 * ListAppend()
 * UnpackElements(before_count=1, has_extras=True, after_count=1)
 * StoreLocal(name="a")  # 1
 * StoreLocal(name="b")  # [2, 3]
 * StoreLocal(name="c")  # 4
 * }</pre>
 *
 * @param beforeCount the number of elements to unpack from the beginning
 * @param hasExtras whether there is a middle list for remaining elements
 * @param afterCount the number of elements to unpack from the end
 */
public record UnpackElements(int beforeCount,
                             boolean hasExtras,
                             int afterCount) implements Opcode {
}
