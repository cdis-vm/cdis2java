package io.github.cdisvm.runtime.builtin;

import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;

public class PyList<T extends PyObject> extends PySequenceBase<T> implements PySettable {
    @Override
    @SuppressWarnings("unchecked")
    public void pySetItem(PyObject key, PyObject value) {
        delegate.set(PyIndexable.wrapping(key).pyIndex().value().intValueExact(), (T) value);
    }
}
