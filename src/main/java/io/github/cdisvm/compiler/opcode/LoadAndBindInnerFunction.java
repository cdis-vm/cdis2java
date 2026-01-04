package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.InnerFunction;

public record LoadAndBindInnerFunction(InnerFunction innerFunction) implements Opcode {
}
