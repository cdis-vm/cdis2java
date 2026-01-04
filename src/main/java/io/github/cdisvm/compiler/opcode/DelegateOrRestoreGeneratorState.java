package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.StackMetadata;

public record DelegateOrRestoreGeneratorState(int stateId,
                                              StackMetadata savedStackMetadata) implements Opcode {
}
