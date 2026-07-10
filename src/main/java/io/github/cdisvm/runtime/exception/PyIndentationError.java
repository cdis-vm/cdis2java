package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyIndentationError extends PySyntaxError {
    public PyIndentationError() {
        super();
    }

    public PyIndentationError(String message) {
        super(message);
    }

    public PyIndentationError(PyStr message) {
        super(message);
    }
}
