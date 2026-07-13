package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("NotADirectoryError")
public class PyNotADirectoryError extends PyOSError {
    public static PyType type;

    public PyNotADirectoryError() {
        super();
    }

    public PyNotADirectoryError(String message) {
        super(message);
    }

    public PyNotADirectoryError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyNotADirectoryError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyNotADirectoryError(message);
    }
}
