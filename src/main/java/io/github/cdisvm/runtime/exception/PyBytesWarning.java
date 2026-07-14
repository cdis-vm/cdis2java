package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("BytesWarning")
public class PyBytesWarning extends PyWarning {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyBytesWarning() {
        super();
    }

    public PyBytesWarning(String message) {
        super(message);
    }

    public PyBytesWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyBytesWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyBytesWarning(message);
    }
}
