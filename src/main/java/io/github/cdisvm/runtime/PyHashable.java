package io.github.cdisvm.runtime;

public interface PyHashable {
    PyObject pyHash();

    static PyHashable wrapping(PyObject maybeHashable) {
        if (maybeHashable instanceof PyHashable pyHashable) {
            return pyHashable;
        }
        return () -> {
            var containsCallable = maybeHashable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__hash__");
            if (containsCallable instanceof PyCallable callable) {
                return callable.pyCallBuilder()
                        .$appendArgument(maybeHashable)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
