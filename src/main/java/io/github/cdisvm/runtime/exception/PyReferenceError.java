package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ReferenceError")
public class PyReferenceError extends PyException {
    public static PyType type;

    public PyReferenceError() {
        super();
    }

    public PyReferenceError(String message) {
        super(message);
    }

    public PyReferenceError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyReferenceError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyReferenceError(message);
    }
}
