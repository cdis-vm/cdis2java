package io.github.cdisvm.runtime;

public interface PyNegatable {
    PyObject pyNegate();

    static PyNegatable wrapping(PyObject maybeNegatable) {
        if (maybeNegatable instanceof PyNegatable pyNegatable) {
            return pyNegatable;
        }
        return () -> {
            var containsCallable = maybeNegatable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__neg__");
            if (containsCallable instanceof PyCallable callable) {
                return callable.pyCallBuilder()
                        .$appendArgument(maybeNegatable)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
