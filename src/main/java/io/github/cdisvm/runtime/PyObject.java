package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyBool;

public interface PyObject {
    default PyAttributes pyAttributes() {
        throw new UnsupportedOperationException();
    }
    default PyType pyType() {
        throw new UnsupportedOperationException();
    }
    default PyBool pyTruth() {
        return PyBool.TRUE;
    }
}
