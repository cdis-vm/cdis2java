package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyHasGreaterThan {
    // Python allows non-bool returns from comparisons
    PyObject pyGreaterThan(PyObject other);

    static PyObject greaterThanResult(PyObject left, PyObject right) {
        if (left instanceof PyHasGreaterThan lhs && right instanceof PyHasLessThan rhs) {
            var result = lhs.pyGreaterThan(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            } else {
                result = rhs.pyLessThan(left);
                if (result != PyNotImplemented.INSTANCE) {
                    return result;
                }
            }
        } else if (left instanceof PyHasGreaterThan lhs) {
            return lhs.pyGreaterThan(right);
        } else if (right instanceof PyHasLessThan rhs) {
            return rhs.pyLessThan(left);
        }
        return PyNotImplemented.INSTANCE;
    }

    static PyObject greaterThan(PyObject left, PyObject right) {
        var greaterThanResult = greaterThanResult(left, right);
        if (greaterThanResult != PyNotImplemented.INSTANCE) {
            return greaterThanResult;
        }
        var lessThanOrEqualResult = PyHasLessThanOrEqual.lessThanOrEqualResult(left, right);
        if (lessThanOrEqualResult != PyNotImplemented.INSTANCE) {
            return lessThanOrEqualResult.pyTruth().negate();
        }
        // TODO: throw TypeError instead
        throw new UnsupportedOperationException();
    }
}
