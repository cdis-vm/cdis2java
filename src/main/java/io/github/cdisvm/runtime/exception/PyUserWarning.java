package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUserWarning extends PyWarning {
    public PyUserWarning() {
        super();
    }

    public PyUserWarning(String message) {
        super(message);
    }

    public PyUserWarning(PyStr message) {
        super(message);
    }
}
