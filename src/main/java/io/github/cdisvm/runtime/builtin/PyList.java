package io.github.cdisvm.runtime.builtin;

import java.util.List;

import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.exception.PyIndexError;

@PyBuiltin("list")
public class PyList<T extends PyObject> extends PySequenceBase<T> implements PySettable {

    public PyList() {
        super();
    }

    public PyList(List<T> delegate) {
        super(delegate);
    }

    @PyConstructor
    public static PyList<?> create() {
        // TODO
        return new PyList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pySetItem(PyObject key, PyObject value) {
        var index = PyIndexable.wrapping(key).pyIndex().intValue();
        if (index < 0) {
            index = delegate.size() + index;
        }
        if (index < 0 || index >= delegate.size()) {
            throw new PyIndexError("Index: " + index + ", Size: " + delegate.size());
        }
        delegate.set(index, (T) value);
    }

    @Override
    public String toString() {
        var out = new StringBuilder();
        out.append('[');
        for (var item : delegate) {
            out.append(item);
            out.append(',');
        }
        if (!delegate.isEmpty()) {
            out.deleteCharAt(out.length() - 1);
        }
        out.append(']');
        return out.toString();
    }
}
