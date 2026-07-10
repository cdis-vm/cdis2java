package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyFutureWarning extends PyWarning {
    public PyFutureWarning() {
        super();
    }

    public PyFutureWarning(String message) {
        super(message);
    }

    public PyFutureWarning(PyStr message) {
        super(message);
    }
}
