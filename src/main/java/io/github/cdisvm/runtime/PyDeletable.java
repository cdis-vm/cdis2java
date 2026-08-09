package io.github.cdisvm.runtime;

public interface PyDeletable {
    void pyDeleteItem(PyObject index);

    static PyDeletable wrapping(PyObject maybeDeletable) {
        if (maybeDeletable instanceof PyDeletable pyDeletable) {
            return pyDeletable;
        }
        return (item) -> {
            var containsCallable = maybeDeletable.pyType()
                    .pyAttributes()
                    .getAttributeByName("__delitem__");
            if (containsCallable instanceof PyCallable callable) {
                callable.pyCallBuilder()
                        .$appendArgument(maybeDeletable)
                        .$appendArgument(item)
                        .pyCall();
            } else {
                throw new UnsupportedOperationException();
            }
        };
    }
}
