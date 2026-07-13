package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("MemoryError")
public class PyMemoryError extends PyException {
    public static PyType type;

    public PyMemoryError() {
        super();
    }

    public PyMemoryError(String message) {
        super(message);
    }

    public PyMemoryError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyMemoryError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyMemoryError(message);
    }
}
