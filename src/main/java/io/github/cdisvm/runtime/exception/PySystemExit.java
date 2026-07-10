package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PySystemExit extends PyBaseException {
    public PySystemExit() {
        super();
    }

    public PySystemExit(String message) {
        super(message);
    }

    public PySystemExit(PyStr message) {
        super(message);
    }
}
