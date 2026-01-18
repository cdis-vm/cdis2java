package io.github.cdisvm.runtime;

public interface PySettable {
    void pySetItem(PyObject key, PyObject value);

    static PySettable wrapping(PyObject maybeSettable) {
        if (maybeSettable instanceof PySettable pySettable) {
            return pySettable;
        }
        return (key, value) -> {
            var containsCallable = maybeSettable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__setitem__");
            if (containsCallable instanceof PyCallable callable) {
                callable.pyCallBuilder()
                        .$appendArgument(maybeSettable)
                        .$appendArgument(key)
                        .$appendArgument(value)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
