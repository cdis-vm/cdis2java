package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

/**
 * Top of stack is the checked type, and the item below it is the queried object.
 * <p>
 * Pop only the checked type off the stack. Jump to target if the object is not an instance of
 * the checked type, or does not have the specified attributes. If positionalCount is non-zero,
 * read {@code __match_args__} from the popped type, and raise {@code TypeError} if positionalCount
 * is greater than len(__match_args__), or if __match_args__ is missing from the type.
 * <p>
 * If the queried object is an instance of the type and has the specified attributes, push the
 * values of the specified attributes to the stack.
 * <p>
 * Stack Effect: len(attributes) + positionalCount - 1 if matched else -1
 * Prior: ..., query, type
 * After (matched): ..., query, positional_0, ..., positional_{positionalCount - 1}, attribute_0, ..., attribute_{len(attributes) - 1}
 * After (not matched): ..., query
 *
 * <pre>{@code
 * >>> match query:
 * ...     case MyType(positional_arg, my_attr=value):
 * ...         pass
 * LoadLocal(name="query")
 * MatchClass(target=no_match, positional_count=1, attributes=('my_attr',))
 * StoreSynthetic(index=0)  # my_attr
 * StoreSynthetic(index=1)  # positional_arg
 * LoadSynthetic(index=0)
 * StoreLocal(name='value')
 * LoadSynthetic(index=1)
 * StoreLocal(name='positional_arg')
 * JumpTo(target=end_match)
 * label no_match
 * Pop()
 * label end_match
 * }</pre>
 *
 * @param attributes the attribute names to extract if the type matches
 * @param positionalCount the number of positional match args to extract
 * @param targetBytecodeIndex where to jump if the type does not match
 */
@NullMarked
public record MatchClass(List<String> attributes,
                         int positionalCount,
                         int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
