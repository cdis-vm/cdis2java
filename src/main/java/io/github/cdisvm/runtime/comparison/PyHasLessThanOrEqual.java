package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyHasLessThanOrEqual {
    // Python allows non-bool returns from comparisons
    PyObject pyLessThanOrEqual(PyObject other);

    static PyObject lessThanOrEqualResult(PyObject left, PyObject right) {
        if (left instanceof PyHasLessThanOrEqual lhs && right instanceof PyHasGreaterThanOrEqual rhs) {
            var result = lhs.pyLessThanOrEqual(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            } else {
                result = rhs.pyGreaterThanOrEqual(left);
                if (result != PyNotImplemented.INSTANCE) {
                    return result;
                }
            }
        } else if (left instanceof PyHasLessThanOrEqual lhs) {
            return lhs.pyLessThanOrEqual(right);
        } else if (right instanceof PyHasGreaterThanOrEqual rhs) {
            return rhs.pyGreaterThanOrEqual(left);
        }
        return PyNotImplemented.INSTANCE;
    }

    static PyObject lessThanOrEqual(PyObject left, PyObject right) {
        var lessThanOrEqualResult = lessThanOrEqualResult(left, right);
        if (lessThanOrEqualResult != PyNotImplemented.INSTANCE) {
            return lessThanOrEqualResult;
        }
        var greaterThanResult = PyHasGreaterThan.greaterThanResult(left, right);
        if (greaterThanResult != PyNotImplemented.INSTANCE) {
            return greaterThanResult.pyTruth().negate();
        }
        // TODO: throw TypeError instead
        throw new UnsupportedOperationException();
    }
}
