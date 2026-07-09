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
        var index = PyIndexable.wrapping(key).pyIndex().intValue();
        if (index < 0) {
            index = delegate.size() + index;
        }
        if (index < 0 || index >= delegate.size()) {
            // TODO: Throw IndexError instead
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + delegate.size());
        }
        delegate.set(index, (T) value);
    }
}
