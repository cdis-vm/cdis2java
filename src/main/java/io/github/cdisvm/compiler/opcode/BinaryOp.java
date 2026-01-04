package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.BinaryOperator;

public record BinaryOp(BinaryOperator operator) implements Opcode {
}
