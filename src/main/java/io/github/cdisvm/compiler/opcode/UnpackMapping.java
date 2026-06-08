package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.runtime.PyObject;

/**
 * Pops off the top of stack (which is a mapping), and pushes the values of the given keys onto
 * the stack in reversed order.
 * <p>
 * If {@code hasExtras} is true, push all items in the mapping not specified by the given keys
 * into a new dict at the top of the stack.
 * <p>
 * Stack Effect: len(keys) + (1 if hasExtras else 0) - 1
 * Prior: ..., mapping
 * After: ..., value_(len(keys) - 1), ..., value_1, value_0, (extras_dict if hasExtras)
 *
 * <pre>{@code
 * >>> match mapping:
 * ...     case {'a': x, 'b': y}:
 * LoadLocal(name="mapping")
 * MatchMapping(keys=("a", "b"))
 * UnpackMapping(keys=("a", "b"), has_extras=False)
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * }</pre>
 *
 * @param keys the keys to extract from the mapping
 * @param hasExtras whether to include remaining keys in an extras dict
 */
@NullMarked
public record UnpackMapping(List<PyObject> keys,
                            boolean hasExtras) implements Opcode {
}
