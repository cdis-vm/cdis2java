package io.github.cdisvm.runtime.builtin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;
import io.github.cdisvm.runtime.PySizable;

public record PyDict<Key_ extends PyObject, Value_ extends PyObject>(SequencedMap<Key_, Value_> delegate) implements
        PyObject, PySizable, PyContainer, PyGettable, PySettable, SequencedMap<Key_, Value_> {
    public PyDict() {
        this(new LinkedHashMap<>());
    }

    public PyDict<Key_, Value_> pyPutAndReturnThis(PyObject key, PyObject value) {
        delegate.put((Key_) key, (Value_) value);
        return this;
    }

    @Override
    public PyBool pyHasItem(PyObject item) {
        return PyBool.of(delegate.containsKey(item));
    }

    @Override
    public PyObject pyGetItem(PyObject item) {
        var out = delegate.get(item);
        if (out == null) {
            // TODO: throw KeyError instead
            throw new RuntimeException();
        }
        return out;
    }

    @Override
    public void pySetItem(PyObject key, PyObject value) {
        delegate.put((Key_) key, (Value_) value);
    }

    @Override
    public PyInt pyLength() {
        return PyInt.of(delegate.size());
    }

    @Override
    public SequencedMap<Key_, Value_> reversed() {
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
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Value_ get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Value_ put(Key_ key, Value_ value) {
        return delegate.put(key, value);
    }

    @Override
    public Value_ remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends Key_, ? extends Value_> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<Key_> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Value_> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<Key_, Value_>> entrySet() {
        return delegate.entrySet();
    }
}
