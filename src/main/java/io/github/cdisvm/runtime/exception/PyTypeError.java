package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("TypeError")
public class PyTypeError extends PyException {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyTypeError() {
        super();
    }

    public PyTypeError(String message) {
        super(message);
    }

    public PyTypeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyTypeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyTypeError(message);
    }
}
