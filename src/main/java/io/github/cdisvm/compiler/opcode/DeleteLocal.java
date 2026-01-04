package io.github.cdisvm.compiler.opcode;

public record DeleteLocal(String localName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return localName;
    }
}
