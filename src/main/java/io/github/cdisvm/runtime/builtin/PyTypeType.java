package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.util.ArrayList;
import java.util.List;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;

public final class PyTypeType implements PyType, PyConstant {
    @PyBuiltin("type")
    public static PyTypeType INSTANCE = new PyTypeType();

    private final List<PyType> MRO;

    private PyTypeType() {
        MRO = List.of(this, PyObjectType.INSTANCE);
    }

    @Override
    public List<PyType> mro() {
        return MRO;
    }

    @Override
    public boolean instanceCheck(PyObject instance) {
        return instance instanceof PyType;
    }

    @Override
    public boolean subclassCheck(PyType clazz) {
        return clazz.mro().contains(this);
    }

    @Override
    public PyCallBuilder pyCallBuilder() {
        // TODO
        return null;
    }

    @Override
    public PyObject pyCall(PyCallBuilder callBuilder) {
        // TODO
        return null;
    }

    @Override
    public PyAttributes pyAttributes() {
        return PyEmptyAttributes.INSTANCE;
    }

    @Override
    public PyType pyType() {
        return this;
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.getstatic(CD.of(PyTypeType.class), "INSTANCE", CD.of(PyTypeType.class));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyTypeType_INSTANCE";
    }
}
