package io.github.cdisvm.runtime.builtin;

import java.util.List;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

public final class PyTypeType implements PyType {
    public static PyTypeType INSTANCE = new PyTypeType();

    private PyTypeType() {}

    @Override
    public List<PyType> mro() {
        return List.of(this, PyObjectType.INSTANCE);
    }

    @Override
    public PyCallBuilder pyCallBuilder() {
        // TODO
        return null;
    }

    @Override
    public PyObject pyCall(PyCallBuilder callBuilder) {
        // TODO
        return null;
    }

    @Override
    public PyAttributes pyAttributes() {
        return PyEmptyAttributes.INSTANCE;
    }

    @Override
    public PyType pyType() {
        return this;
    }
}
