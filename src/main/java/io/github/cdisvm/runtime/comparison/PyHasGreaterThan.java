package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;

public interface PyHasGreaterThan {
    // Python allows non-bool returns from comparisons
    PyObject pyGreaterThan(PyObject other);
}
