package io.github.cdisvm.compiler.opcode;

/**
 * Deletes a global variable.
 * <p>
 * If a global variable has the same name as a builtin, it does not delete the builtin.
 * <p>
 * Raises a {@code NameError} if the global variable is not defined.
 * <p>
 * Stack Effect: 0
 * Prior: ...
 * After: ...
 *
 * <pre>{@code
 * >>> global x
 * ... del x
 * DeleteGlobal(name="x")
 * }</pre>
 *
 * @param globalName the name of the global variable
 */
public record DeleteGlobal(String globalName) implements Opcode {
}
