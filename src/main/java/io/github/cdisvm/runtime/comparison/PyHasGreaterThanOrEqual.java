package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;

public interface PyHasGreaterThanOrEqual {
    // Python allows non-bool returns from comparisons
    PyObject pyGreaterThanOrEqual(PyObject other);
}
