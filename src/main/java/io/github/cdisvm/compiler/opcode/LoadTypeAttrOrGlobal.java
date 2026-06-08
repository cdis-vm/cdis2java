package io.github.cdisvm.compiler.opcode;

/**
 * TOS is a mapping. If the specified variable exists in TOS, load it.
 * <p>
 * Otherwise load the global variable or builtin with that name onto the stack.
 * <p>
 * Stack Effect: 0
 * Prior: ..., mapping
 * After: ..., local_or_global
 *
 * <pre>{@code
 * >>> class A:
 * ...     a = id
 * LoadSynthetic(index=0)
 * LoadTypeAttrOrGlobal(name="id")
 * StoreLocal(name="a")
 * }</pre>
 *
 * @param name the name of the type attribute
 */
public record LoadTypeAttrOrGlobal(String name) implements Opcode {
}
