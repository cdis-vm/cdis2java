package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyBytesWarning extends PyWarning {
    public PyBytesWarning() {
        super();
    }

    public PyBytesWarning(String message) {
        super(message);
    }

    public PyBytesWarning(PyStr message) {
        super(message);
    }
}
