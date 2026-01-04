package io.github.cdisvm.compiler.opcode;

public record StoreCell(String cellName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return cellName;
    }
}
