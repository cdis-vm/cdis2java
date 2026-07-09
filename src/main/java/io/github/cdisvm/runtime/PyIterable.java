package io.github.cdisvm.runtime;

public interface PyIterable {
    PyIterator pyIterator();

    static PyIterable wrapping(PyObject maybeGettable) {
        if (maybeGettable instanceof PyIterable pyGettable) {
            return pyGettable;
        }
        return () -> {
            var containsCallable = maybeGettable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__iter__");
            if (containsCallable instanceof PyCallable callable) {
                return (PyIterator) callable.pyCallBuilder()
                        .$appendArgument(maybeGettable)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
