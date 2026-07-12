package io.github.cdisvm.runtime.builtin;

import java.util.Collections;
import java.util.List;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.annotation.PyBuiltin;

@PyBuiltin("tuple")
public class PyTuple<T extends PyObject> extends PySequenceBase<T> {
    private static final PyTuple<?> EMPTY = new PyTuple<>(Collections.emptyList());

    public PyTuple() {
        super(Collections.emptyList());
    }

    public PyTuple(List<T> delegate) {
        super(Collections.unmodifiableList(delegate));
    }

    @SuppressWarnings("unchecked")
    public static <Item_ extends PyObject> PyTuple<Item_> empty() {
        return (PyTuple<Item_>) EMPTY;
    }

    @SafeVarargs
    public static <Item_ extends PyObject> PyTuple<Item_> of(Item_... items) {
        return new PyTuple<>(List.of(items));
    }

    @Override
    public String toString() {
        var out = new StringBuilder();
        out.append('(');
        for (var item : delegate) {
            out.append(item);
            out.append(',');
        }
        if (delegate.size() > 1) {
            out.deleteCharAt(out.length() - 1);
        }
        out.append(')');
        return out.toString();
    }
}
