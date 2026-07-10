package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyRuntimeWarning extends PyWarning {
    public PyRuntimeWarning() {
        super();
    }

    public PyRuntimeWarning(String message) {
        super(message);
    }

    public PyRuntimeWarning(PyStr message) {
        super(message);
    }
}
