package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("UnicodeError")
public class PyUnicodeError extends PyValueError {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyUnicodeError() {
        super();
    }

    public PyUnicodeError(String message) {
        super(message);
    }

    public PyUnicodeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUnicodeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUnicodeError(message);
    }
}
