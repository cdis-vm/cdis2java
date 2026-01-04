package io.github.cdisvm.compiler.opcode;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.runtime.PyObject;

@NullMarked
public record MatchMapping(List<PyObject> keys,
                           int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }
}
