package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.StackMetadata;

public record SaveGeneratorState(int stateId, StackMetadata savedStackMetadata) implements Opcode {
}
