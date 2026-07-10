package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyPendingDeprecationWarning extends PyWarning {
    public PyPendingDeprecationWarning() {
        super();
    }

    public PyPendingDeprecationWarning(String message) {
        super(message);
    }

    public PyPendingDeprecationWarning(PyStr message) {
        super(message);
    }
}
