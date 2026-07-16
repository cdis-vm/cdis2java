package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.util.List;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.exception.PyValueError;

public final class PyObjectType implements PyType, PyConstant {
    @PyBuiltin("object")
    public static PyObjectType INSTANCE = new PyObjectType();

    private final List<PyType> MRO;

    private PyObjectType() {
        MRO = List.of(this);
    }

    @Override
    public List<PyType> mro() {
        return MRO;
    }

    @Override
    public boolean instanceCheck(PyObject instance) {
        return true;
    }

    @Override
    public boolean subclassCheck(PyType clazz) {
        return true;
    }

    @Override
    public PyCallBuilder pyCallBuilder() {
        return new PyCallBuilder() {
            @Override
            public PyObject pyCall() {
                return new PyObject() {
                    @Override
                    public PyAttributes pyAttributes() {
                        return PyEmptyAttributes.INSTANCE;
                    }

                    @Override
                    public PyType pyType() {
                        return PyObjectType.INSTANCE;
                    }
                };
            }

            @Override
            public PyCallBuilder $appendArgument(PyObject argument) {
                throw new PyValueError("Too many arguments");
            }

            @Override
            public PyCallBuilder $putArgument(String argumentName, PyObject argument) {
                throw new PyValueError("Too many arguments");
            }
        };
    }

    @Override
    public PyObject pyCall(PyCallBuilder callBuilder) {
        return pyCallBuilder().pyCall();
    }

    @Override
    public PyType pyType() {
        return PyTypeType.INSTANCE;
    }

    @Override
    public PyAttributes pyAttributes() {
        return PyEmptyAttributes.INSTANCE;
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.getstatic(CD.of(PyObjectType.class), "INSTANCE", CD.of(PyObjectType.class));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyObjectType_INSTANCE";
    }
}
