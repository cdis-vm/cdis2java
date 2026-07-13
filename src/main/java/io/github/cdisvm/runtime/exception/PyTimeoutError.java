package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("TimeoutError")
public class PyTimeoutError extends PyOSError {
    public PyTimeoutError() {
        super();
    }

    public PyTimeoutError(String message) {
        super(message);
    }

    public PyTimeoutError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyTimeoutError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyTimeoutError(message);
    }
}
