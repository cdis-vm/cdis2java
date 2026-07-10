package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyTabError extends PyIndentationError {
    public PyTabError() {
        super();
    }

    public PyTabError(String message) {
        super(message);
    }

    public PyTabError(PyStr message) {
        super(message);
    }
}
