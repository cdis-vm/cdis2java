package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;

public interface PyHasLessThanOrEqual {
    // Python allows non-bool returns from comparisons
    PyObject pyLessThanOrEqual(PyObject other);
}
