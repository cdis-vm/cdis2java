package io.github.cdisvm.runtime.exception;

import org.jspecify.annotations.Nullable;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyVarArgs;
import io.github.cdisvm.runtime.builtin.PyEmptyAttributes;
import io.github.cdisvm.runtime.builtin.PyList;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.builtin.PyTuple;

@PyBuiltin("BaseException")
public class PyBaseException extends RuntimeException implements PyObject {
    public final PyTuple<PyObject> args;
    @Nullable
    public PyList<PyStr> __notes__;
    public static PyType type;

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

    @PyConstructor
    public static PyBaseException create(@PyVarArgs PyTuple<PyObject> args) {
        return new PyBaseException(args);
    }

    public void add_note(PyStr note) {
        if (__notes__ == null) {
            __notes__ = new PyList<>();
        }
        __notes__.add(note);
    }

    @Override
    public PyAttributes pyAttributes() {
        return PyEmptyAttributes.INSTANCE;
    }

    @Override
    public PyType pyType() {
        return type;
    }
}
