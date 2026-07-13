package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("IndentationError")
public class PyIndentationError extends PySyntaxError {
    public PyIndentationError() {
        super();
    }

    public PyIndentationError(String message) {
        super(message);
    }

    public PyIndentationError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyIndentationError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyIndentationError(message);
    }
}
