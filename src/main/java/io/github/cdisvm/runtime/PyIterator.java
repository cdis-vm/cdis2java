package io.github.cdisvm.runtime;

import org.jspecify.annotations.Nullable;

public interface PyIterator extends PyIterable {
    @Override
    default PyIterator pyIterator() {
        return this;
    }

    /**
     * @return the next object from the iterator, or null if the iterator empty
     */
    @Nullable PyObject pyNext();
}
