package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.nio.ByteBuffer;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.util.ByteCharSequence;

@PyBuiltin("bytearray")
public record PyByteArray(ByteBuffer valueBuffer) implements PyConstant {
    public static PyType type;

    @PyConstructor
    public static PyByteArray create() {
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
        return PyBool.of(valueBuffer.limit() == 0);
    }

    public CharSequence asCharSequence() {
        return new ByteCharSequence(valueBuffer.array(), 0, valueBuffer.limit());
    }

}
