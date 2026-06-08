package io.github.cdisvm.compiler.opcode;

/**
 * Deletes a local variable.
 * <p>
 * The local variable is not a cell variable (a variable shared with another function) or a
 * synthetic variable (a variable introduced by the compiler).
 * <p>
 * Raises {@code UnboundLocalError} if the local variable is not defined yet.
 * <p>
 * Stack Effect: 0
 * Prior: ...
 * After: ...
 *
 * <pre>{@code
 * >>> del x
 * DeleteLocal(name="x")
 * }</pre>
 *
 * @param localName the name of the local variable
 */
public record DeleteLocal(String localName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return localName;
    }
}
