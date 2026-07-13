package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("FloatingPointError")
public class PyFloatingPointError extends PyArithmeticError {
    public static PyType type;

    public PyFloatingPointError() {
        super();
    }

    public PyFloatingPointError(String message) {
        super(message);
    }

    public PyFloatingPointError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyFloatingPointError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyFloatingPointError(message);
    }
}
