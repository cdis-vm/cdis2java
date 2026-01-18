package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.math.BigInteger;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

@NullMarked
public record PyInt(BigInteger value) implements PyObject, PyIndexable, PyConstant {
    private static final int CACHE_START = -10;
    private static final int CACHE_END = 256;
    private static final PyInt[] CACHE = generateCache();

    private static PyInt[] generateCache() {
        var cache = new PyInt[CACHE_END - CACHE_START + 1];
        for (var i = CACHE_START; i <= CACHE_END; i++) {
            cache[i - CACHE_START] = new PyInt(BigInteger.valueOf(i));
        }
        return cache;
    }

    public static PyInt of(int value) {
        if (value < CACHE_START || value > CACHE_END) {
            return new PyInt(BigInteger.valueOf(value));
        }
        return CACHE[value - CACHE_START];
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        var pyIntClassDesc = ClassDesc.of(PyInt.class.getCanonicalName());
        var bigIntClassDesc = ClassDesc.of(BigInteger.class.getCanonicalName());
        var stringClassDesc = ClassDesc.of(String.class.getCanonicalName());

        try {
            var intValue = value.intValueExact();
            codeBuilder.loadConstant(intValue);
            codeBuilder.invokestatic(pyIntClassDesc, "of",
                    MethodTypeDesc.of(pyIntClassDesc, CD.INT));
        } catch (ArithmeticException e) {
            codeBuilder.new_(pyIntClassDesc);
            codeBuilder.dup();
            codeBuilder.new_(bigIntClassDesc);
            codeBuilder.dup();
            codeBuilder.loadConstant(value.toString());
            codeBuilder.invokespecial(bigIntClassDesc, "<init>",
                    MethodTypeDesc.of(CD.VOID, stringClassDesc));
            codeBuilder.invokespecial(pyIntClassDesc, "<init>",
                    MethodTypeDesc.of(CD.VOID, bigIntClassDesc));
        }
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyInt_" + value.toString().replace('-', '$');
    }

    @Override
    public PyInt pyIndex() {
        return this;
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
        return PyBool.of(value.compareTo(BigInteger.ZERO) != 0);
    }
}
