package io.github.cdisvm.compiler.opcode;

import java.util.stream.Stream;

public interface HasCellGroup {
    Stream<String> getCells();
}
