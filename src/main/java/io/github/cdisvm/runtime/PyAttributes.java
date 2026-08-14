package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.exception.PyAttributeError;

public interface PyAttributes {
    default PyObject getAttributeByName(String name) {
        var out = getAttributeByNameOrNull(name);
        if (out == null) {
            throw new PyAttributeError("'%s' object has no attribute '%s'".formatted(this, name));
        }
        return out;
    }
    PyObject getAttributeByNameOrNull(String name);
    void setAttributeByName(String name, PyObject value);
    void deleteAttributeByName(String name);
}
