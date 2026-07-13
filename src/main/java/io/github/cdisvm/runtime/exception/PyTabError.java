package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("TabError")
public class PyTabError extends PyIndentationError {
    public PyTabError() {
        super();
    }

    public PyTabError(String message) {
        super(message);
    }

    public PyTabError(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyTabError create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyTabError(message);
    }
}
