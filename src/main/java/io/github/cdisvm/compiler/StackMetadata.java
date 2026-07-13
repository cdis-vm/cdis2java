package io.github.cdisvm.compiler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record StackMetadata(List<ValueSource> stack,
                            Map<String, ValueSource> localVariables,
                            List<ValueSource> syntheticVariables,
                            boolean isDead) {
    public static StackMetadata empty() {
        return new StackMetadata(Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyList(),
                false);
    }
}
