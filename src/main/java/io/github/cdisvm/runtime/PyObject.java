package io.github.cdisvm.runtime;

import java.util.IdentityHashMap;

import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyDefaultAttributes;
import io.github.cdisvm.runtime.builtin.PyStr;

public interface PyObject {
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
}
