package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("SyntaxWarning")
public class PySyntaxWarning extends PyWarning {
    public static PyType type;

    public PySyntaxWarning() {
        super();
    }

    public PySyntaxWarning(String message) {
        super(message);
    }

    public PySyntaxWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PySyntaxWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PySyntaxWarning(message);
    }
}
