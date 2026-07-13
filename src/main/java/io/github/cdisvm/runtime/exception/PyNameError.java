package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("NameError")
public class PyNameError extends PyException {
    public static PyType type;

    public PyNameError() {
        super();
    }

    public PyNameError(String message) {
        super(message);
    }

    public PyNameError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyNameError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyNameError(message);
    }
}
