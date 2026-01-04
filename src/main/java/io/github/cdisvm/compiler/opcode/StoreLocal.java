package io.github.cdisvm.compiler.opcode;

public record StoreLocal(String localName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return localName;
    }
}
