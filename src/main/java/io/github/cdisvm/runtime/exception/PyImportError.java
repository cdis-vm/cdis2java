package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("ImportError")
public class PyImportError extends PyException {
    public PyImportError() {
        super();
    }

    public PyImportError(String message) {
        super(message);
    }

    public PyImportError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyImportError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyImportError(message);
    }
}
