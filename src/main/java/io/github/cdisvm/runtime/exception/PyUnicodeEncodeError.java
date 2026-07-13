package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("UnicodeEncodeError")
public class PyUnicodeEncodeError extends PyUnicodeError {
    public static PyType type;

    public PyUnicodeEncodeError() {
        super();
    }

    public PyUnicodeEncodeError(String message) {
        super(message);
    }

    public PyUnicodeEncodeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUnicodeEncodeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUnicodeEncodeError(message);
    }
}
