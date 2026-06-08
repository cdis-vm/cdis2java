package io.github.cdisvm.compiler.opcode;

/**
 * Deletes a cell variable.
 * <p>
 * A cell variable is a variable shared with another function. They are typically implemented by
 * creating a holder object called a cell, then reading/modifying an attribute of the cell to
 * read/set the variable.
 * <p>
 * Raises {@code NameError} if the cell variable is a free variable and undefined, and
 * {@code UnboundLocalError} if the cell variable is not defined and not a free variable.
 * <p>
 * Stack Effect: 0
 * Prior: ...
 * After: ...
 *
 * <pre>{@code
 * >>> nonlocal x
 * ... del x
 * DeleteCell(name="x")
 * }</pre>
 *
 * @param cellName the name of the cell variable
 */
public record DeleteCell(String cellName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return cellName;
    }
}
