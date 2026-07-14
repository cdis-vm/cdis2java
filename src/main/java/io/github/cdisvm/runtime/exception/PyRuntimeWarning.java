package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("RuntimeWarning")
public class PyRuntimeWarning extends PyWarning {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyRuntimeWarning() {
        super();
    }

    public PyRuntimeWarning(String message) {
        super(message);
    }

    public PyRuntimeWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyRuntimeWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyRuntimeWarning(message);
    }
}
