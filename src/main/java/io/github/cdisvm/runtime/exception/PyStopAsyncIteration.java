package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("StopAsyncIteration")
public class PyStopAsyncIteration extends PyException {
    public static PyType type;

    public PyStopAsyncIteration() {
        super();
    }

    public PyStopAsyncIteration(String message) {
        super(message);
    }

    public PyStopAsyncIteration(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyStopAsyncIteration create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyStopAsyncIteration(message);
    }
}
