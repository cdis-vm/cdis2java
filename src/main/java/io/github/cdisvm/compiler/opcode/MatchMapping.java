package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.runtime.PyObject;

/**
 * Top of stack is the queried object.
 * <p>
 * Do not pop it off the stack, and check if it is a mapping with the given keys. If it is not
 * a mapping with the given keys, jump to target.
 * <p>
 * Stack Effect: 0
 * Prior: ..., query
 * After: ..., query
 *
 * <pre>{@code
 * >>> match query:
 * ...     case {'a': x, 'b': y}:
 * ...         pass
 * LoadLocal(name="query")
 * MatchMapping(keys=("a", "b"), target=no_match)
 * UnpackMapping(keys=("a", "b"), has_extras=False, target=no_match)
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * JumpTo(target=end_match)
 * label no_match
 * Pop()
 * label end_match
 * }</pre>
 *
 * @param keys the keys that must be present in the mapping
 * @param targetBytecodeIndex where to jump if the mapping does not contain the keys
 */
@NullMarked
public record MatchMapping(List<PyObject> keys,
                           int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
