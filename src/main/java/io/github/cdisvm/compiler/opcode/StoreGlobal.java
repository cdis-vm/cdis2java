package io.github.cdisvm.compiler.opcode;

/**
 * Stores the value at the top of the stack into a global variable.
 * <p>
 * If a global variable has the same name as a builtin, it does not overwrite the builtin.
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> global x
 * ... x = 10
 * LoadConstant(constant=10)
 * StoreGlobal(name="x")
 * }</pre>
 *
 * @param globalName the name of the global variable
 */
public record StoreGlobal(String globalName) implements Opcode {
}
