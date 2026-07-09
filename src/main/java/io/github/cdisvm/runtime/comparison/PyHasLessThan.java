package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;

public interface PyHasLessThan {
    // Python allows non-bool returns from comparisons
    PyObject pyLessThan(PyObject other);
}
