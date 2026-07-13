package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("FileExistsError")
public class PyFileExistsError extends PyOSError {
    public PyFileExistsError() {
        super();
    }

    public PyFileExistsError(String message) {
        super(message);
    }

    public PyFileExistsError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyFileExistsError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyFileExistsError(message);
    }
}
