package io.github.cdisvm.compiler.opcode;

/**
 * Stores the value at the top of stack into a cell variable.
 * <p>
 * A cell variable is a variable shared with another function. They are typically implemented by
 * creating a holder object called a cell, then reading/modifying an attribute of the cell to
 * read/set the variable.
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> nonlocal x
 * ... x = 0
 * LoadConstant(constant=0)
 * StoreCell(name="x")
 * }</pre>
 *
 * @param cellName the name of the cell variable
 */
public record StoreCell(String cellName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return cellName;
    }
}
