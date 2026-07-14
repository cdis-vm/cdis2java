package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("FutureWarning")
public class PyFutureWarning extends PyWarning {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyFutureWarning() {
        super();
    }

    public PyFutureWarning(String message) {
        super(message);
    }

    public PyFutureWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyFutureWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyFutureWarning(message);
    }
}
