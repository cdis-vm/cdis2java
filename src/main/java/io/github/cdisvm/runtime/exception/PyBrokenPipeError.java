package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("BrokenPipeError")
public class PyBrokenPipeError extends PyConnectionError {
    public PyBrokenPipeError() {
        super();
    }

    public PyBrokenPipeError(String message) {
        super(message);
    }

    public PyBrokenPipeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyBrokenPipeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyBrokenPipeError(message);
    }
}
