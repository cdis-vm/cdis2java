package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("UnicodeWarning")
public class PyUnicodeWarning extends PyWarning {
    public static PyType type;

    public PyUnicodeWarning() {
        super();
    }

    public PyUnicodeWarning(String message) {
        super(message);
    }

    public PyUnicodeWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUnicodeWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUnicodeWarning(message);
    }
}
