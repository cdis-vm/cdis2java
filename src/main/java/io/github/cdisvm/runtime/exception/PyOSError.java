package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("OSError")
public class PyOSError extends PyException {
    public PyOSError() {
        super();
    }

    public PyOSError(String message) {
        super(message);
    }

    public PyOSError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyOSError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyOSError(message);
    }
}
