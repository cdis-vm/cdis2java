package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyPythonFinalizationError extends PyRuntimeError {
    public PyPythonFinalizationError() {
        super();
    }

    public PyPythonFinalizationError(String message) {
        super(message);
    }

    public PyPythonFinalizationError(PyStr message) {
        super(message);
    }
}
