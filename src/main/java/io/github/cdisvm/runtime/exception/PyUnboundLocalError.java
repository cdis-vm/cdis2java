package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("UnboundLocalError")
public class PyUnboundLocalError extends PyNameError {
    public static PyType type;

    public PyUnboundLocalError() {
        super();
    }

    public PyUnboundLocalError(String message) {
        super(message);
    }

    public PyUnboundLocalError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUnboundLocalError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUnboundLocalError(message);
    }
}
