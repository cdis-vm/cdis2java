package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyResourceWarning extends PyWarning {
    public PyResourceWarning() {
        super();
    }

    public PyResourceWarning(String message) {
        super(message);
    }

    public PyResourceWarning(PyStr message) {
        super(message);
    }
}
