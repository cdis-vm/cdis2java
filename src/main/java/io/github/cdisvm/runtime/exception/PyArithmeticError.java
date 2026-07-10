package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyArithmeticError extends PyException {
    public PyArithmeticError() {
        super();
    }

    public PyArithmeticError(String message) {
        super(message);
    }

    public PyArithmeticError(PyStr message) {
        super(message);
    }
}
