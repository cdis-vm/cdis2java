package io.github.cdisvm.runtime;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

public record PyDelegatingIterator(Iterator<? extends PyObject> delegate) implements PyObject, PyIterator {
    @Override
    public @Nullable PyObject pyNext() {
        if (delegate.hasNext()) {
            return delegate.next();
        }
        return null;
    }
}
