package io.github.cdisvm.compiler.opcode;

public record DeleteCell(String cellName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return cellName;
    }
}
