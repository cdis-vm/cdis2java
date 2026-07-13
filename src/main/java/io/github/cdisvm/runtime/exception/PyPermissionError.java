package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("PermissionError")
public class PyPermissionError extends PyOSError {
    public PyPermissionError() {
        super();
    }

    public PyPermissionError(String message) {
        super(message);
    }

    public PyPermissionError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyPermissionError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyPermissionError(message);
    }
}
