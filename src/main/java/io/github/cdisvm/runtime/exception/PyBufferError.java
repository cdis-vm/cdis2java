package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("BufferError")
public class PyBufferError extends PyException {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyBufferError() {
        super();
    }

    public PyBufferError(String message) {
        super(message);
    }

    public PyBufferError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyBufferError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyBufferError(message);
    }
}
