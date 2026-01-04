package io.github.cdisvm.compiler;

import java.util.List;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record InnerFunction(Bytecode bytecode,
                            Bytecode annotateFunction,
                            List<String> argumentsWithDefaultValues) {
}
