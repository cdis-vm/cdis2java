package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;

@PyBuiltin("GeneratorExit")
public class PyGeneratorExit extends PyBaseException {
    public PyGeneratorExit() {
        super();
    }

    @PyConstructor
    public static PyGeneratorExit create() {
        return new PyGeneratorExit();
    }
}
