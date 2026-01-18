package io.github.cdisvm.runtime;

public interface PyGettable {
    PyObject pyGetItem(PyObject item);

    static PyGettable wrapping(PyObject maybeGettable) {
        if (maybeGettable instanceof PyGettable pyGettable) {
            return pyGettable;
        }
        return (item) -> {
            var containsCallable = maybeGettable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__getitem__");
            if (containsCallable instanceof PyCallable callable) {
                return callable.pyCallBuilder()
                        .$appendArgument(maybeGettable)
                        .$appendArgument(item)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
