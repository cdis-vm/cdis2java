package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("AttributeError")
public class PyAttributeError extends PyException {
    public PyAttributeError() {
        super();
    }

    public PyAttributeError(String message) {
        super(message);
    }

    public PyAttributeError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyAttributeError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyAttributeError(message);
    }
}
