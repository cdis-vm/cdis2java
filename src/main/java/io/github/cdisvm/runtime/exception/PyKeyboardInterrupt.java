package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("KeyboardInterrupt")
public class PyKeyboardInterrupt extends PyBaseException {
    public static PyType type;

    public PyKeyboardInterrupt() {
        super();
    }

    public PyKeyboardInterrupt(String message) {
        super(message);
    }

    public PyKeyboardInterrupt(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyKeyboardInterrupt create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyKeyboardInterrupt(message);
    }
}
