package io.github.cdisvm.compiler.opcode;

public record MatchSequence(int length,
                            boolean isExact,
                            int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
