package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("FileNotFoundError")
public class PyFileNotFoundError extends PyOSError {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyFileNotFoundError() {
        super();
    }

    public PyFileNotFoundError(String message) {
        super(message);
    }

    public PyFileNotFoundError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyFileNotFoundError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyFileNotFoundError(message);
    }
}
