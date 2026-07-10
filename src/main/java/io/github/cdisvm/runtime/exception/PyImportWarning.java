package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyImportWarning extends PyWarning {
    public PyImportWarning() {
        super();
    }

    public PyImportWarning(String message) {
        super(message);
    }

    public PyImportWarning(PyStr message) {
        super(message);
    }
}
