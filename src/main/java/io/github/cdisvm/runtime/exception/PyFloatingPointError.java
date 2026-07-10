package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyFloatingPointError extends PyArithmeticError {
    public PyFloatingPointError() {
        super();
    }

    public PyFloatingPointError(String message) {
        super(message);
    }

    public PyFloatingPointError(PyStr message) {
        super(message);
    }
}
