package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;

@PyBuiltin("bool")
public record PyBool(boolean value) implements PyConstant {
    @PyBuiltin("True")
    public static final PyBool TRUE = new PyBool(true);
    @PyBuiltin("False")
    public static final PyBool FALSE = new PyBool(false);

    public static PyBool of(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        var boolClassDesc = ClassDesc.of(PyBool.class.getCanonicalName());
        if (value) {
            codeBuilder.getstatic(boolClassDesc,
                    "TRUE",
                    boolClassDesc);
        } else {
            codeBuilder.getstatic(boolClassDesc,
                    "FALSE",
                    boolClassDesc);
        }
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyBool_" + value;
    }

    @Override
    public PyAttributes pyAttributes() {
        return null;
    }

    @Override
    public PyType pyType() {
        return null;
    }

    @Override
    public PyBool pyTruth() {
        return this;
    }

    public PyBool negate() {
        return value? FALSE : TRUE;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
