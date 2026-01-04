package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.ClassInfo;

public record LoadAndBindInnerGenerator(ClassInfo innerGenerator) implements Opcode {
}
