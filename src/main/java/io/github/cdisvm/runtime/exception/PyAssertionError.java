package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("AssertionError")
public class PyAssertionError extends PyException {
    public static PyType type;

    public PyAssertionError() {
        super();
    }

    public PyAssertionError(String message) {
        super(message);
    }

    public PyAssertionError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyAssertionError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyAssertionError(message);
    }
}
