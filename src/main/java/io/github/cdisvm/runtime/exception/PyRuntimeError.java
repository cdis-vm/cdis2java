package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("RuntimeError")
public class PyRuntimeError extends PyException {
    public PyRuntimeError() {
        super();
    }

    public PyRuntimeError(String message) {
        super(message);
    }

    public PyRuntimeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyRuntimeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyRuntimeError(message);
    }
}
