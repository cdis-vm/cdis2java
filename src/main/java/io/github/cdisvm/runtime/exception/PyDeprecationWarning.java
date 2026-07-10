package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyDeprecationWarning extends PyWarning {
    public PyDeprecationWarning() {
        super();
    }

    public PyDeprecationWarning(String message) {
        super(message);
    }

    public PyDeprecationWarning(PyStr message) {
        super(message);
    }
}
