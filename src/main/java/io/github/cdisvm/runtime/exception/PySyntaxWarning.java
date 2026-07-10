package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PySyntaxWarning extends PyWarning {
    public PySyntaxWarning() {
        super();
    }

    public PySyntaxWarning(String message) {
        super(message);
    }

    public PySyntaxWarning(PyStr message) {
        super(message);
    }
}
