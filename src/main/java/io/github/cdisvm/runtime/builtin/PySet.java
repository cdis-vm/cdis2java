package io.github.cdisvm.runtime.builtin;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySizable;
import io.github.cdisvm.runtime.annotation.PyBuiltin;

@PyBuiltin("set")
public record PySet<Item_ extends PyObject>(SequencedSet<Item_> delegate) implements
        PyObject, PySizable, PyContainer, SequencedSet<Item_> {
    public PySet() {
        this(new LinkedHashSet<>());
    }

    @Override
    public PyBool pyHasItem(PyObject item) {
        return PyBool.of(delegate.contains(item));
    }

    @Override
    public PyInt pyLength() {
        return PyInt.of(delegate.size());
    }

    @Override
    public SequencedSet<Item_> reversed() {
        return delegate.reversed();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<Item_> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(Item_ item) {
        return delegate.add(item);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Item_> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public String toString() {
        var out = new StringBuilder();
        out.append('{');
        for (var item : delegate) {
            out.append(item);
            out.append(',');
        }
        if (!delegate.isEmpty()) {
            out.deleteCharAt(out.length() - 1);
        }
        out.append('}');
        return out.toString();
    }
}
