package io.github.cdisvm.runtime.comparison;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyHasGreaterThanOrEqual {
    // Python allows non-bool returns from comparisons
    PyObject pyGreaterThanOrEqual(PyObject other);

    static PyObject greaterThanOrEqualResult(PyObject left, PyObject right) {
        if (left instanceof PyHasGreaterThanOrEqual lhs && right instanceof PyHasLessThanOrEqual rhs) {
            var result = lhs.pyGreaterThanOrEqual(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            } else {
                result = rhs.pyLessThanOrEqual(left);
                if (result != PyNotImplemented.INSTANCE) {
                    return result;
                }
            }
        } else if (left instanceof PyHasGreaterThanOrEqual lhs) {
            return lhs.pyGreaterThanOrEqual(right);
        } else if (right instanceof PyHasLessThanOrEqual rhs) {
            return rhs.pyLessThanOrEqual(left);
        }
        return PyNotImplemented.INSTANCE;
    }

    static PyObject greaterThanOrEqual(PyObject left, PyObject right) {
        var greaterThanOrEqualResult = greaterThanOrEqualResult(left, right);
        if (greaterThanOrEqualResult != PyNotImplemented.INSTANCE) {
            return greaterThanOrEqualResult;
        }
        var lessThanResult = PyHasLessThan.lessThanResult(left, right);
        if (lessThanResult != PyNotImplemented.INSTANCE) {
            return lessThanResult.pyTruth().negate();
        }
        // TODO: throw TypeError instead
        throw new UnsupportedOperationException();
    }
}
