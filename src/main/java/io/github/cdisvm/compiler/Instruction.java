package io.github.cdisvm.compiler;

import io.github.cdisvm.compiler.opcode.Opcode;

public record Instruction(
        Opcode opcode,
        int bytecodeIndex,
        int sourceLineNumber
) {
}
