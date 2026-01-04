package io.github.cdisvm.compiler;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

@NullMarked
public record FunctionParameter(
        int parameterIndex,
        String parameterName,
        ParameterKind parameterKind,
        PyType parameterType,
        @Nullable PyObject defaultValue
        ) {
}
