package io.github.cdisvm.runtime;

import java.util.List;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PyType extends PyObject, PyCallable {
    static PyType of(Class<? extends PyObject> clazz) {
        return null; // TODO: Implement
    }

    List<PyType> mro();

    boolean instanceCheck(PyObject instance);
    boolean subclassCheck(PyType clazz);
}
