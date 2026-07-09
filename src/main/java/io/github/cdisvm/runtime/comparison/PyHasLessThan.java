package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyHasLessThan {
    // Python allows non-bool returns from comparisons
    PyObject pyLessThan(PyObject other);

    static PyObject lessThanResult(PyObject left, PyObject right) {
        if (left instanceof PyHasLessThan lhs && right instanceof PyHasGreaterThan rhs) {
            var result = lhs.pyLessThan(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            } else {
                result = rhs.pyGreaterThan(left);
                if (result != PyNotImplemented.INSTANCE) {
                    return result;
                }
            }
        } else if (left instanceof PyHasLessThan lhs) {
            return lhs.pyLessThan(right);
        } else if (right instanceof PyHasGreaterThan rhs) {
            return rhs.pyGreaterThan(left);
        }
        return PyNotImplemented.INSTANCE;
    }

    static PyObject lessThan(PyObject left, PyObject right) {
        var lessThanResult = lessThanResult(left, right);
        if (lessThanResult != PyNotImplemented.INSTANCE) {
            return lessThanResult;
        }
        var greaterThanOrEqualResult = PyHasGreaterThanOrEqual.greaterThanOrEqualResult(left, right);
        if (greaterThanOrEqualResult != PyNotImplemented.INSTANCE) {
            return greaterThanOrEqualResult.pyTruth().negate();
        }
        // TODO: throw TypeError instead
        throw new UnsupportedOperationException();
    }
}
