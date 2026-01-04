package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.FormatConversion;

public record FormatValue(FormatConversion conversion, String formatSpec) implements Opcode {
}
