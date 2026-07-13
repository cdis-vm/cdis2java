package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("KeyError")
public class PyKeyError extends PyLookupError {
    public PyKeyError() {
        super();
    }

    public PyKeyError(String message) {
        super(message);
    }

    public PyKeyError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyKeyError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyKeyError(message);
    }
}
