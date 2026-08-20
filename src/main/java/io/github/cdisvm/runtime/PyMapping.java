package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.comparison.PyHasEquals;

public interface PyMapping extends PyIterable, PyGettable, PySizable, PyContainer {
    default PyBool pyHasItem(PyObject item) {
        var keyIterator = pyIterator();
        var next = keyIterator.pyNext();
        while (next != null) {
            if (PyHasEquals.equal(item, next).pyTruth().value()) {
                return PyBool.TRUE;
            }
            next = keyIterator.pyNext();
        }
        return PyBool.FALSE;
    }
}
