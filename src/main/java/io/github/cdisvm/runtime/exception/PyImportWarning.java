package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("ImportWarning")
public class PyImportWarning extends PyWarning {
    public PyImportWarning() {
        super();
    }

    public PyImportWarning(String message) {
        super(message);
    }

    public PyImportWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyImportWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyImportWarning(message);
    }
}
