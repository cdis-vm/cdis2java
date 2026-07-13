package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("Warning")
public class PyWarning extends PyException {
    public static PyType type;

    public PyWarning() {
        super();
    }

    public PyWarning(String message) {
        super(message);
    }

    public PyWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyWarning(message);
    }
}
