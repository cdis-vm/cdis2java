package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

/**
 * Push the module with the given name to the stack.
 * <p>
 * The module is loaded and executed if it is not loaded yet. Raises {@code ImportError} if the
 * module cannot be found.
 * <p>
 * Used to implement import statements.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., module
 *
 * <pre>{@code
 * >>> import cdis
 * ImportModule(name='cdis', level=0, from_list=())
 * }</pre>
 *
 * @param name the name of the module to import
 * @param level the import level (0 for absolute, positive for relative)
 * @param fromList the list of names to import from the module
 */
@NullMarked
public record ImportModule(String name,
                           int level,
                           List<String> fromList) implements Opcode {
}
