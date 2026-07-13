package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("InterruptedError")
public class PyInterruptedError extends PyOSError {
    public PyInterruptedError() {
        super();
    }

    public PyInterruptedError(String message) {
        super(message);
    }

    public PyInterruptedError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyInterruptedError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyInterruptedError(message);
    }
}
