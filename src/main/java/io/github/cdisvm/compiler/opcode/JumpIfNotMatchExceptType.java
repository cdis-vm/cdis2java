package io.github.cdisvm.compiler.opcode;

public record JumpIfNotMatchExceptType(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
