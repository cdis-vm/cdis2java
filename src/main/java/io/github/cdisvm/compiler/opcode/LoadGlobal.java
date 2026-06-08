package io.github.cdisvm.compiler.opcode;

/**
 * Loads a global variable or builtin onto the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., global
 *
 * <pre>{@code
 * >>> global x
 * ... x
 * LoadGlobal(name="x")
 *
 * >>> int
 * LoadGlobal(name="int")
 * }</pre>
 *
 * @param globalName the name of the global variable or builtin
 */
public record LoadGlobal(String globalName) implements Opcode {
}
