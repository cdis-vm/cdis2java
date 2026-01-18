package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyInt;

public interface PyIndexable {
    PyInt pyIndex();

    static PyIndexable wrapping(PyObject maybeIndexable) {
        if (maybeIndexable instanceof PyIndexable pyIndexable) {
            return pyIndexable;
        }
        return () -> {
            var containsCallable = maybeIndexable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__index__");
            if (containsCallable instanceof PyCallable callable) {
                return (PyInt) callable.pyCallBuilder()
                        .$appendArgument(maybeIndexable)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
