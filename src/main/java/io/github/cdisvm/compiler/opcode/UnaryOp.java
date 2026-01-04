package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.UnaryOperator;

public record UnaryOp(UnaryOperator operator) implements Opcode {
}
