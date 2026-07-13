package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("PendingDeprecationWarning")
public class PyPendingDeprecationWarning extends PyWarning {
    public PyPendingDeprecationWarning() {
        super();
    }

    public PyPendingDeprecationWarning(String message) {
        super(message);
    }

    public PyPendingDeprecationWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyPendingDeprecationWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyPendingDeprecationWarning(message);
    }
}
