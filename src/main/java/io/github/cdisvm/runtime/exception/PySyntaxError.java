package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("SyntaxError")
public class PySyntaxError extends PyException {
    public static PyType type;

    public PySyntaxError() {
        super();
    }

    public PySyntaxError(String message) {
        super(message);
    }

    public PySyntaxError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PySyntaxError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PySyntaxError(message);
    }
}
