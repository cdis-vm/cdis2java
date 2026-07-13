package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("UnicodeTranslateError")
public class PyUnicodeTranslateError extends PyUnicodeError {
    public static PyType type;

    public PyUnicodeTranslateError() {
        super();
    }

    public PyUnicodeTranslateError(String message) {
        super(message);
    }

    public PyUnicodeTranslateError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUnicodeTranslateError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUnicodeTranslateError(message);
    }
}
