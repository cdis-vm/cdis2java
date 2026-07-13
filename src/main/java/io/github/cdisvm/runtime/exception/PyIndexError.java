package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("IndexError")
public class PyIndexError extends PyLookupError {
    public static PyType type;

    public PyIndexError() {
        super();
    }

    public PyIndexError(String message) {
        super(message);
    }

    public PyIndexError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyIndexError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyIndexError(message);
    }
}
