package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUnicodeWarning extends PyWarning {
    public PyUnicodeWarning() {
        super();
    }

    public PyUnicodeWarning(String message) {
        super(message);
    }

    public PyUnicodeWarning(PyStr message) {
        super(message);
    }
}
