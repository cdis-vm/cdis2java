package io.github.cdisvm.runtime;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PyType extends PyObject {
    static PyType of(Class<? extends PyObject> clazz) {
        return null; // TODO: Implement
    }
}
