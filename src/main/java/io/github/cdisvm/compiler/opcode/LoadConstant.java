package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.runtime.PyConstant;

public record LoadConstant(PyConstant constant) implements Opcode {
}
