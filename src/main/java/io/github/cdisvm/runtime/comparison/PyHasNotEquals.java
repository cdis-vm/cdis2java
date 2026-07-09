package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyHasNotEquals {
    // Python allows non-bool returns from comparisons
    PyObject pyNotEquals(PyObject other);

    static PyObject notEqualResult(PyObject left, PyObject right) {
        if (left instanceof PyHasNotEquals lhs && right instanceof PyHasNotEquals rhs) {
            var result = lhs.pyNotEquals(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            } else {
                result = rhs.pyNotEquals(left);
                if (result != PyNotImplemented.INSTANCE) {
                    return result;
                }
            }
        } else if (left instanceof PyHasNotEquals lhs) {
            return lhs.pyNotEquals(right);
        } else if (right instanceof PyHasNotEquals rhs) {
            return rhs.pyNotEquals(left);
        }
        return PyNotImplemented.INSTANCE;
    }

    static PyObject notEqual(PyObject left, PyObject right) {
        var notEqualResult = notEqualResult(left, right);
        if (notEqualResult != PyNotImplemented.INSTANCE) {
            return notEqualResult;
        }
        var equalResult = PyHasEquals.equalResult(left, right);
        if (equalResult != PyNotImplemented.INSTANCE) {
            return equalResult.pyTruth().negate();
        }
        return PyBool.of(left !=  right);
    }
}
