package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ConnectionAbortedError")
public class PyConnectionAbortedError extends PyConnectionError {
    public static PyType type;

    public PyConnectionAbortedError() {
        super();
    }

    public PyConnectionAbortedError(String message) {
        super(message);
    }

    public PyConnectionAbortedError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyConnectionAbortedError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyConnectionAbortedError(message);
    }
}
