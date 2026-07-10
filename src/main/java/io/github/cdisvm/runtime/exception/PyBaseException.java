package io.github.cdisvm.runtime.exception;

import org.jspecify.annotations.Nullable;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyList;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.builtin.PyTuple;

public class PyBaseException extends RuntimeException {
    public final PyTuple<PyObject> args;
    @Nullable
    public PyList<PyStr> __notes__;

    public PyBaseException() {
        super();
        this.args = PyTuple.empty();
    }

    public PyBaseException(String message) {
        super(message);
        args = PyTuple.of(new PyStr(message));
    }

    public PyBaseException(PyStr message) {
        super(message.value());
        args = PyTuple.of(message);
    }

    public PyBaseException(PyTuple<PyObject> args) {
        super(args.isEmpty()? "" : args.toString());
        this.args = args;
    }

    public void add_note(PyStr note) {
        if (__notes__ == null) {
            __notes__ = new PyList<>();
        }
        __notes__.add(note);
    }
}
