package io.github.cdisvm.runtime;

public interface PyHasPos {
    PyObject pyPositive();

    static PyHasPos wrapping(PyObject maybeHasPos) {
        if (maybeHasPos instanceof PyHasPos pyHasPos) {
            return pyHasPos;
        }
        return () -> {
            var containsCallable = maybeHasPos.pyType()
                    .pyAttributes()
                    .getAttributeByName("__pos__");
            if (containsCallable instanceof PyCallable callable) {
                return callable.pyCallBuilder()
                        .$appendArgument(maybeHasPos)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
