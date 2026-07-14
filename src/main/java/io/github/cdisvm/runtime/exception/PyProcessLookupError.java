package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ProcessLookupError")
public class PyProcessLookupError extends PyOSError {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyProcessLookupError() {
        super();
    }

    public PyProcessLookupError(String message) {
        super(message);
    }

    public PyProcessLookupError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyProcessLookupError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyProcessLookupError(message);
    }
}
