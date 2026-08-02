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
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.exception.PyTypeError;
import io.github.cdisvm.runtime.util.ByteCharSequence;

@PyBuiltin("bytes")
public record PyBytes(byte[] value) implements PyConstant {
    public static PyType type;

    @PyConstructor
    public static PyBytes create() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJavaIdentifierName() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public PyAttributes pyAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyType pyType() {
        return type;
    }

    @Override
    public PyBool pyTruth() {
        return PyBool.of(value.length == 0);
    }

    public CharSequence asCharSequence() {
        return new ByteCharSequence(value);
    }

}
