package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("EOFError")
public class PyEOFError extends PyException {
    public static PyType type;

    public PyEOFError() {
        super();
    }

    public PyEOFError(String message) {
        super(message);
    }

    public PyEOFError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyEOFError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyEOFError(message);
    }
}
