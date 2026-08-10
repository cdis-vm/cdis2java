package io.github.cdisvm.runtime.builtin;

import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;

public class PyUserTypeCallBuilder implements PyCallBuilder {
    private final PyObject newInstance;
    public PyUserTypeCallBuilder(PyObject newInstance) {
        this.newInstance = newInstance;
    }

    @Override
    public PyObject pyCall() {
        return newInstance;
    }

    @Override
    public PyCallBuilder $bindTo(PyObject binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyCallBuilder $returning(PyObject value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyCallBuilder $appendArgument(PyObject argument) {
        return this;
    }

    @Override
    public PyCallBuilder $putArgument(String argumentName, PyObject argument) {
        return this;
    }
}
