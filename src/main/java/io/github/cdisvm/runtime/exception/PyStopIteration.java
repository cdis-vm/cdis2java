package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("StopIteration")
public class PyStopIteration extends PyException {
    public static PyType type;

    public PyStopIteration() {
        super();
    }

    public PyStopIteration(String message) {
        super(message);
    }

    public PyStopIteration(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyStopIteration create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyStopIteration(message);
    }
}
