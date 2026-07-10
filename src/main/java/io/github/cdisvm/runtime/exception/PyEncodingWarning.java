package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyEncodingWarning extends PyWarning {
    public PyEncodingWarning() {
        super();
    }

    public PyEncodingWarning(String message) {
        super(message);
    }

    public PyEncodingWarning(PyStr message) {
        super(message);
    }
}
