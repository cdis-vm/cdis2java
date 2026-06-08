package io.github.cdisvm.compiler.opcode;

/**
 * Loads a cell variable onto the stack.
 * <p>
 * A cell variable is a variable shared with another function. They are typically implemented by
 * creating a holder object called a cell, then reading/modifying an attribute of the cell to
 * read/set the variable.
 * <p>
 * Raises {@code NameError} if the cell variable is a free variable and undefined, and
 * {@code UnboundLocalError} if the cell variable is not defined and not a free variable.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., cell_value
 *
 * <pre>{@code
 * >>> nonlocal x
 * ... x
 * LoadCell(name="x")
 * }</pre>
 *
 * @param cellName the name of the cell variable
 */
public record LoadCell(String cellName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return cellName;
    }
}
