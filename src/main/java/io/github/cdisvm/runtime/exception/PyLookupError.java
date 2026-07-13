package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("LookupError")
public class PyLookupError extends PyException {
    public PyLookupError() {
        super();
    }

    public PyLookupError(String message) {
        super(message);
    }

    public PyLookupError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyLookupError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyLookupError(message);
    }
}
