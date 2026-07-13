package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("UnicodeDecodeError")
public class PyUnicodeDecodeError extends PyUnicodeError {
    public PyUnicodeDecodeError() {
        super();
    }

    public PyUnicodeDecodeError(String message) {
        super(message);
    }

    public PyUnicodeDecodeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUnicodeDecodeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUnicodeDecodeError(message);
    }
}
