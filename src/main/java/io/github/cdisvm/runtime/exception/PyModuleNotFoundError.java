package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyModuleNotFoundError extends PyImportError {
    public PyModuleNotFoundError() {
        super();
    }

    public PyModuleNotFoundError(String message) {
        super(message);
    }

    public PyModuleNotFoundError(PyStr message) {
        super(message);
    }
}
