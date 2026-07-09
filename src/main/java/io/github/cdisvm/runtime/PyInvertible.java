package io.github.cdisvm.runtime;

public interface PyInvertible {
    PyObject pyInvert();

    static PyInvertible wrapping(PyObject maybeInvertible) {
        if (maybeInvertible instanceof PyInvertible pyInvertible) {
            return pyInvertible;
        }
        return () -> {
            var containsCallable = maybeInvertible.pyType()
                    .pyAttributes()
                    .getAttributeByName("__invert__");
            if (containsCallable instanceof PyCallable callable) {
                return callable.pyCallBuilder()
                        .$appendArgument(maybeInvertible)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
