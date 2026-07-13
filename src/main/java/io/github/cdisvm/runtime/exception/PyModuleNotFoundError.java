package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ModuleNotFoundError")
public class PyModuleNotFoundError extends PyImportError {
    public static PyType type;

    public PyModuleNotFoundError() {
        super();
    }

    public PyModuleNotFoundError(String message) {
        super(message);
    }

    public PyModuleNotFoundError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyModuleNotFoundError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyModuleNotFoundError(message);
    }
}
