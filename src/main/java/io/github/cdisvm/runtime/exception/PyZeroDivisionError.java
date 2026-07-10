package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyZeroDivisionError extends PyArithmeticError {
    public PyZeroDivisionError() {
        super();
    }

    public PyZeroDivisionError(String message) {
        super(message);
    }

    public PyZeroDivisionError(PyStr message) {
        super(message);
    }
}
