package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.ClassInfo;

/**
 * Loads and binds an inner generator.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., bound_inner_generator
 *
 * @param innerGenerator the inner generator class info
 */
public record LoadAndBindInnerGenerator(ClassInfo innerGenerator) implements Opcode {
}
