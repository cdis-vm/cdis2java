package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ConnectionError")
public class PyConnectionError extends PyOSError {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyConnectionError() {
        super();
    }

    public PyConnectionError(String message) {
        super(message);
    }

    public PyConnectionError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyConnectionError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyConnectionError(message);
    }
}
