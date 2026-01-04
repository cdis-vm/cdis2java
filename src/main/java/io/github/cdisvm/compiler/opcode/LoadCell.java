package io.github.cdisvm.compiler.opcode;

public record LoadCell(String cellName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return cellName;
    }
}
