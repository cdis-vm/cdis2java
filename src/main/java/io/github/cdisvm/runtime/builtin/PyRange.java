package io.github.cdisvm.runtime.builtin;

import java.util.Iterator;

import io.github.cdisvm.runtime.PyDelegatingIterator;
import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyIterator;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.annotation.PyPosOnly;

@PyBuiltin("range")
public record PyRange(PyInt start, PyInt end, PyInt step) implements PyIterable {
    @PyConstructor
    public static PyRange create(@PyPosOnly PyIndexable endOrStart,
            @PyPosOnly @PyDefault(type=PyDefault.Type.NULL, value="") PyIndexable end,
            @PyPosOnly @PyDefault(type=PyDefault.Type.INT, value="1") PyIndexable step) {
        if (end == null) {
            return new PyRange(PyInt.of(0), endOrStart.pyIndex(), step.pyIndex());
        } else {
            return new PyRange(endOrStart.pyIndex(), end.pyIndex(), step.pyIndex());
        }
    }

    @Override
    public PyIterator pyIterator() {
        return new PyDelegatingIterator(new Iterator<>() {
            PyInt current = start;

            @Override
            public boolean hasNext() {
                return PyBool.TRUE == current.pyLessThan(end);
            }

            @Override
            public PyObject next() {
                var saved = current;
                current = (PyInt) current.pyAdd(step);
                return saved;
            }
        });
    }
}
