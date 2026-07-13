package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("NotImplementedError")
public class PyNotImplementedError extends PyRuntimeError {
    public PyNotImplementedError() {
        super();
    }

    public PyNotImplementedError(String message) {
        super(message);
    }

    public PyNotImplementedError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyNotImplementedError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyNotImplementedError(message);
    }
}
