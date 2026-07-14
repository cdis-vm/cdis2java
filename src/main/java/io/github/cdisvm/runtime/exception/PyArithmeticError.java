package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ArithmeticError")
public class PyArithmeticError extends PyException {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyArithmeticError() {
        super();
    }

    public PyArithmeticError(String message) {
        super(message);
    }

    public PyArithmeticError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyArithmeticError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyArithmeticError(message);
    }
}
