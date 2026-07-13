package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.builtin.PyStr;

@PyBuiltin("EncodingWarning")
public class PyEncodingWarning extends PyWarning {
    public PyEncodingWarning() {
        super();
    }

    public PyEncodingWarning(String message) {
        super(message);
    }

    public PyEncodingWarning(PyStr message) {
        super(message);
    }

    @PyConstructor
    public static PyEncodingWarning create(
            @PyDefault(type=PyDefault.Type.STRING, value="") PyStr message) {
        return new PyEncodingWarning(message);
    }
}
