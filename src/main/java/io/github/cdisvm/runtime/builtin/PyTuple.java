package io.github.cdisvm.runtime.builtin;

import java.util.List;

import io.github.cdisvm.runtime.PyObject;

public class PyTuple<T extends PyObject> extends PySequenceBase<T> {
    public PyTuple() {
    }

    public PyTuple(List<T> delegate) {
        super(delegate);
    }
}
