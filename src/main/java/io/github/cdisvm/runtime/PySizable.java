package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyInt;

public interface PySizable {
    PyInt pyLength();

    static PySizable wrapping(PyObject maybeSizable) {
        if (maybeSizable instanceof PySizable pySizable) {
            return pySizable;
        }
        return () -> {
            var containsCallable = maybeSizable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__len__");
            if (containsCallable instanceof PyCallable callable) {
                return (PyInt) callable.pyCallBuilder()
                        .$appendArgument(maybeSizable)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
