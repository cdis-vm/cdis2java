package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyStr;

public interface PyFormattable {
    PyStr pyFormat(PyObject formatSpec);

    static PyFormattable wrapping(PyObject maybeFormattable) {
        if (maybeFormattable instanceof PyFormattable pyFormattable) {
            return pyFormattable;
        }
        return maybeFormattable.pyString();
    }
}
