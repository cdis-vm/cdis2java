package io.github.cdisvm.runtime.builtin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyDelegatingIterator;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyIterator;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;
import io.github.cdisvm.runtime.PySizable;
import io.github.cdisvm.runtime.PyType;

@NullMarked
public class PySequenceBase<T extends PyObject> implements PyObject, PyGettable, PyContainer,
        PySizable, PyIterable, List<T> {
    final List<T> delegate;

    public PySequenceBase() {
        delegate = new ArrayList<>();
    }

    public PySequenceBase(List<T> delegate) {
        this.delegate = delegate;
    }

    public List<T> getDelegate() {
        return delegate;
    }

    @Override
    public PyAttributes pyAttributes() {
        return null;
    }

    @Override
    public PyType pyType() {
        return null;
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
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return delegate.add(t);
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
    public boolean addAll(Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return delegate.addAll(index, c);
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
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
    }

    @Override
    public T remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public PyBool pyHasItem(PyObject item) {
        return PyBool.of(delegate.contains(item));
    }

    @Override
    public PyObject pyGetItem(PyObject item) {
        var index = PyIndexable.wrapping(item).pyIndex().intValue();
        if (index < 0) {
            index = delegate.size() + index;
        }
        if (index < 0 || index >= delegate.size()) {
            // TODO: Throw IndexError instead
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + delegate.size());
        }
        return delegate.get(index);
    }

    @Override
    public PyInt pyLength() {
        return PyInt.of(delegate.size());
    }

    @Override
    public PyBool pyTruth() {
        return PyBool.of(delegate.isEmpty());
    }

    @Override
    public PyIterator pyIterator() {
        return new PyDelegatingIterator(delegate.iterator());
    }
}
