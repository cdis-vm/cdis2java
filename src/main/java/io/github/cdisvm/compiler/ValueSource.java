package io.github.cdisvm.compiler;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.runtime.PyType;

@NullMarked
public record ValueSource(List<Instruction> sources, PyType type) {
}
