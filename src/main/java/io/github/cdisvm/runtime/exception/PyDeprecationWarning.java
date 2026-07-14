package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("DeprecationWarning")
public class PyDeprecationWarning extends PyWarning {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyDeprecationWarning() {
        super();
    }

    public PyDeprecationWarning(String message) {
        super(message);
    }

    public PyDeprecationWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyDeprecationWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyDeprecationWarning(message);
    }
}
