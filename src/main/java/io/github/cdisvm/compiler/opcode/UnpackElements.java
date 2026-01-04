package io.github.cdisvm.compiler.opcode;

public record UnpackElements(int beforeCount,
                             boolean hasExtras,
                             int afterCount) implements Opcode {
}
