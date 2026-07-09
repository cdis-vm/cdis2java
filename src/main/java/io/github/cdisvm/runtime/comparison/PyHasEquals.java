package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyHasEquals {
    // Python allows non-bool returns from comparisons
    PyObject pyEquals(PyObject other);

    static PyObject equalResult(PyObject left, PyObject right) {
        if (left instanceof PyHasEquals lhs && right instanceof PyHasEquals rhs) {
            var result = lhs.pyEquals(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            } else {
                result = rhs.pyEquals(left);
                if (result != PyNotImplemented.INSTANCE) {
                    return result;
                }
            }
        } else if (left instanceof PyHasEquals lhs) {
            return lhs.pyEquals(right);
        } else if (right instanceof PyHasEquals rhs) {
            return rhs.pyEquals(left);
        }
        return PyNotImplemented.INSTANCE;
    }

    static PyObject equal(PyObject left, PyObject right) {
        var equalResult = equalResult(left, right);
        if (equalResult != PyNotImplemented.INSTANCE) {
            return equalResult;
        }
        var notEqualResult = PyHasNotEquals.notEqualResult(left, right);
        if (notEqualResult != PyNotImplemented.INSTANCE) {
            return notEqualResult.pyTruth().negate();
        }
        return PyBool.of(left ==  right);
    }
}
