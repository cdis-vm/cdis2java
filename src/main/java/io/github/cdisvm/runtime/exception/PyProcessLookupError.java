package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyProcessLookupError extends PyOSError {
    public PyProcessLookupError() {
        super();
    }

    public PyProcessLookupError(String message) {
        super(message);
    }

    public PyProcessLookupError(PyStr message) {
        super(message);
    }
}
