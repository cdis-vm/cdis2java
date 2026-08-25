package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CDisCompiler;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;

@PyBuiltin("tuple")
public class PyTuple<T extends PyObject> extends PySequenceBase<T> implements PyConstant {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    @Override
    PyObject createSlice(PySlice slice) {
        return new PyTuple<>(slice.copyImmutableSliceFromList(delegate));
    }

    private static final PyTuple<?> EMPTY = new PyTuple<>(Collections.emptyList());

    @PyConstructor
    public static PyTuple<?> create() {
        //TODO
        return EMPTY;
    }

    public PyTuple() {
        super(Collections.emptyList());
    }

    public PyTuple(List<T> delegate) {
        super(Collections.unmodifiableList(delegate));
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.new_(CD.PY_TUPLE);
        codeBuilder.dup();

        codeBuilder.new_(CD.of(ArrayList.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(ArrayList.class), "<init>", MD.of(void.class));

        for (var value : delegate) {
            codeBuilder.dup();
            if (value instanceof PyConstant constant) {
                constant.loadValueOntoStack(codeBuilder);
            } else {
                // Might be generic/defined in C, init to null
                codeBuilder.aconst_null();
            }
            codeBuilder.invokeinterface(CD.of(List.class), "add", MD.of(boolean.class, Object.class));
            codeBuilder.pop();
        }

        codeBuilder.invokespecial(CD.PY_TUPLE, "<init>", MD.of(void.class, List.class));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyTuple_" + CDisCompiler.arbitraryTextToJavaIdentifierName(delegate.toString());
    }

    @SuppressWarnings("unchecked")
    public static <Item_ extends PyObject> PyTuple<Item_> empty() {
        return (PyTuple<Item_>) EMPTY;
    }

    @SafeVarargs
    public static <Item_ extends PyObject> PyTuple<Item_> of(Item_... items) {
        return new PyTuple<>(List.of(items));
    }

    @Override
    public String toString() {
        var out = new StringBuilder();
        out.append('(');
        for (var item : delegate) {
            out.append(item.pyRepr().value());
            out.append(", ");
        }
        if (delegate.size() > 1) {
            out.deleteCharAt(out.length() - 1);
            out.deleteCharAt(out.length() - 1);
        }
        out.append(')');
        return out.toString();
    }
}
