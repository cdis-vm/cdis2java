package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.Bytecode;

/**
 * Top of stack is keyword arguments, and the item below it are the tuple of base types.
 * <p>
 * Loads and binds an inner class.
 * <p>
 * Stack Effect: -1
 * Prior: ..., bases, keyword_args
 * After: ..., bound_inner_class
 *
 * @param className the name of the inner class
 * @param classBodyBytecode the bytecode for the class body
 */
public record LoadAndBindInnerClass(String className,
                                    Bytecode classBodyBytecode) implements Opcode {
}
