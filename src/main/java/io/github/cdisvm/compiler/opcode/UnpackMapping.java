package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.runtime.PyObject;

@NullMarked
public record UnpackMapping(List<PyObject> keys,
                            boolean hasExtras) implements Opcode {
}
