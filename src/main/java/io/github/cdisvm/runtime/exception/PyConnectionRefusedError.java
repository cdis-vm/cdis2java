package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("ConnectionRefusedError")
public class PyConnectionRefusedError extends PyConnectionError {
    public PyConnectionRefusedError() {
        super();
    }

    public PyConnectionRefusedError(String message) {
        super(message);
    }

    public PyConnectionRefusedError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyConnectionRefusedError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyConnectionRefusedError(message);
    }
}
