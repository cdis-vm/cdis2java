package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CDisCompiler;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.exception.PyTypeError;

public record PyStr(String value) implements PyConstant, PyContainer, PyAddable {
    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.new_(ClassDesc.of(PyStr.class.getCanonicalName()));
        codeBuilder.dup();
        codeBuilder.loadConstant(value);
        codeBuilder.invokespecial(ClassDesc.of(PyStr.class.getCanonicalName()), "<init>",
                MethodTypeDesc.of(CD.VOID, ClassDesc.of(String.class.getCanonicalName())));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyStr_" + CDisCompiler.arbitraryTextToJavaIdentifierName(value);
    }

    @Override
    public PyAttributes pyAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyType pyType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyBool pyTruth() {
        return PyBool.of(value.isEmpty());
    }

    @Override
    public PyBool pyHasItem(PyObject item) {
        if (!(item instanceof PyStr otherStr)) {
            throw new PyTypeError();
        }
        return PyBool.of(value.contains(otherStr.value));
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public PyObject pyAdd(PyObject other) {
        if (other instanceof PyStr) {
            return new PyStr(value.concat(((PyStr) other).value));
        }
        return PyNotImplemented.INSTANCE;
    }
}
