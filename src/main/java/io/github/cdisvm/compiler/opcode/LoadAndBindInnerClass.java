package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.Bytecode;

public record LoadAndBindInnerClass(String className,
                                    Bytecode classBodyBytecode) implements Opcode {
}
