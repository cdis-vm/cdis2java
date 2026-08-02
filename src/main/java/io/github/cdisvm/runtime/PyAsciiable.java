package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyStr;

public interface PyAsciiable {
    PyStr pyAscii();

    static PyAsciiable wrapping(PyObject maybeAsciible) {
        if (maybeAsciible instanceof PyAsciiable pyAsciiable) {
            return pyAsciiable;
        }
        return maybeAsciible.pyString();
    }
}
