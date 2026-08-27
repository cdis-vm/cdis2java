package io.github.cdisvm.runtime;

import java.util.IdentityHashMap;

import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyDefaultAttributes;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.comparison.PyHasLessThan;

public interface PyObject extends Comparable<PyObject> {
    // TODO: actually implement this inside the class
    IdentityHashMap<PyObject, PyAttributes> objectToAttributeMap = new IdentityHashMap<>();

    default PyAttributes pyAttributes() {
        return objectToAttributeMap.computeIfAbsent(this, _ -> new PyDefaultAttributes());
    }
    default PyType pyType() {
        throw new UnsupportedOperationException();
    }
    default PyBool pyTruth() {
        return PyBool.TRUE;
    }
    default PyStr pyString() {
        return new PyStr(toString());
    }
    default PyStr pyRepr() {
        return new PyStr(toString());
    }
    default int compareTo(PyObject other) {
        var lessThanResult = PyHasLessThan.lessThanResult(this, other);
        if (lessThanResult == PyNotImplemented.INSTANCE) {
            throw new UnsupportedOperationException(this + " cannot be compared with " + other);
        }
        if (lessThanResult.pyTruth().value()) {
            return -1;
        }
        lessThanResult = PyHasLessThan.lessThanResult(other, this);
        if (lessThanResult == PyNotImplemented.INSTANCE) {
            throw new UnsupportedOperationException(other + " cannot be compared with " + this);
        }
        if (lessThanResult.pyTruth().value()) {
            return 1;
        }
        return 0;
    }
}
