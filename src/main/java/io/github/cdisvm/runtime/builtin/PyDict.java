package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CDisCompiler;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyDeletable;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;
import io.github.cdisvm.runtime.PySizable;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.exception.PyKeyError;

@PyBuiltin("dict")
public record PyDict<Key_ extends PyObject, Value_ extends PyObject>(SequencedMap<Key_, Value_> delegate) implements
        PyObject, PySizable, PyContainer, PyGettable, PySettable, PyDeletable, SequencedMap<Key_, Value_>,
        PyConstant {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyDict() {
        this(new LinkedHashMap<>());
    }

    @PyConstructor
    public static PyDict<?, ?> create() {
        // TODO
        return new PyDict<>();
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.new_(CD.PY_DICT);
        codeBuilder.dup();

        codeBuilder.new_(CD.of(LinkedHashMap.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(LinkedHashMap.class), "<init>", MD.of(void.class));

        for (var entry : delegate.entrySet()) {
            codeBuilder.dup();
            if (entry.getKey() instanceof PyConstant key) {
                key.loadValueOntoStack(codeBuilder);
            } else {
                throw new UnsupportedOperationException("Unsupported type: " + entry.getKey());
            }
            if (entry.getValue() instanceof PyConstant value) {
                value.loadValueOntoStack(codeBuilder);
            } else {
                throw new UnsupportedOperationException("Unsupported type: " + entry.getKey());
            }
            codeBuilder.invokeinterface(CD.of(Map.class), "put", MD.of(Object.class, Object.class, Object.class));
            codeBuilder.pop();
        }
        codeBuilder.invokespecial(CD.of(PyDict.class), "<init>", MD.of(void.class, SequencedMap.class));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyDict_" + CDisCompiler.arbitraryTextToJavaIdentifierName(delegate.toString());
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
            throw new PyKeyError();
        }
        return out;
    }

    @Override
    public void pySetItem(PyObject key, PyObject value) {
        delegate.put((Key_) key, (Value_) value);
    }

    @Override
    public void pyDeleteItem(PyObject index) {
        if (delegate.remove(index) == null) {
            throw new PyKeyError(index.pyString().value());
        }
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
