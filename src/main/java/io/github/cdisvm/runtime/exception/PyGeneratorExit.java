package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("GeneratorExit")
public class PyGeneratorExit extends PyBaseException {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyGeneratorExit() {
        super();
    }

    @PyConstructor
    public static PyGeneratorExit create() {
        return new PyGeneratorExit();
    }
}
