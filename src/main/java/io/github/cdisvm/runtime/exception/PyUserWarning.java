package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("UserWarning")
public class PyUserWarning extends PyWarning {
    public static PyType type;

    public PyUserWarning() {
        super();
    }

    public PyUserWarning(String message) {
        super(message);
    }

    public PyUserWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyUserWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyUserWarning(message);
    }
}
