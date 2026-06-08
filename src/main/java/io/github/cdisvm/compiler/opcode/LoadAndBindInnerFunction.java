package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.InnerFunction;

/**
 * Loads and binds an inner function.
 * <p>
 * The inner function's default values are expected to be on the stack in the order given by
 * {@code innerFunction.parametersWithDefaults()}.
 * <p>
 * Stack Effect: 1 - len(innerFunction.parametersWithDefaults())
 * Prior: ..., default1, default2, ..., defaultN
 * After: ..., bound_inner_function
 *
 * @param innerFunction the inner function definition
 */
public record LoadAndBindInnerFunction(InnerFunction innerFunction) implements Opcode {
}
