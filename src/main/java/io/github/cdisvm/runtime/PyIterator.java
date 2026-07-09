package io.github.cdisvm.runtime;

import org.jspecify.annotations.Nullable;

public interface PyIterator {
    /**
     * @return the next object from the iterator, or null if the iterator empty
     */
    @Nullable PyObject pyNext();
}
