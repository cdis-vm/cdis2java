package io.github.cdisvm.runtime.builtin;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.exception.PyAttributeError;

public final class PyEmptyAttributes implements PyAttributes {
    public static final PyEmptyAttributes INSTANCE = new PyEmptyAttributes();

    private PyEmptyAttributes() {}

    @Override
    public PyObject getAttributeByNameOrNull(String name) {
        return null;
    }

    @Override
    public void setAttributeByName(String name, PyObject value) {
        throw new PyAttributeError();
    }

    @Override
    public void deleteAttributeByName(String name) {
        throw new PyAttributeError();
    }
}
