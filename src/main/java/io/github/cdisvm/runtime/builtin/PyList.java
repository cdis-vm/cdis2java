package io.github.cdisvm.runtime.builtin;

import java.util.List;

import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;

public class PyList<T extends PyObject> extends PySequenceBase<T> implements PySettable {

    public PyList() {
        super();
    }

    public PyList(List<T> delegate) {
        super(delegate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pySetItem(PyObject key, PyObject value) {
        delegate.set(PyIndexable.wrapping(key).pyIndex().intValue(), (T) value);
    }
}
