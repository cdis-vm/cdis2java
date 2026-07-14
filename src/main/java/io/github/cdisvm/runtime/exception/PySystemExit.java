package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("SystemExit")
public class PySystemExit extends PyBaseException {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PySystemExit() {
        super();
    }

    public PySystemExit(String message) {
        super(message);
    }

    public PySystemExit(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PySystemExit create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PySystemExit(message);
    }
}
