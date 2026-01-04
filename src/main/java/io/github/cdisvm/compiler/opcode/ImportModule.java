package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record ImportModule(String name,
                           int level,
                           List<String> fromList) implements Opcode {
}
