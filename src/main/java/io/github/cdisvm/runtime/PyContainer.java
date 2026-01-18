package io.github.cdisvm.runtime;

import io.github.cdisvm.runtime.builtin.PyBool;

public interface PyContainer {
    PyBool pyHasItem(PyObject item);

    static PyContainer wrapping(PyObject maybeContainer) {
        if (maybeContainer instanceof PyContainer pyContainer) {
            return pyContainer;
        }
        return (item) -> {
            var containsCallable = maybeContainer.pyType()
                .pyAttributes()
                .getAttributeByName("__contains__");
            if (containsCallable instanceof PyCallable callable) {
                return (PyBool) callable.pyCallBuilder()
                        .$appendArgument(maybeContainer)
                        .$appendArgument(item)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
