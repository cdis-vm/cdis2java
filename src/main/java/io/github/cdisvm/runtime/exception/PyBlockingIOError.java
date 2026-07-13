package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("BlockingIOError")
public class PyBlockingIOError extends PyOSError {
    public PyBlockingIOError() {
        super();
    }

    public PyBlockingIOError(String message) {
        super(message);
    }

    public PyBlockingIOError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyBlockingIOError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyBlockingIOError(message);
    }
}
