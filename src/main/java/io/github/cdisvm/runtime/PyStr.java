package io.github.cdisvm.runtime;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.cdisvm.compiler.CDisCompiler;

public record PyStr(String value) implements PyConstant {
    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.new_(ClassDesc.of(PyStr.class.getCanonicalName()));
        codeBuilder.dup();
        codeBuilder.loadConstant(value);
        codeBuilder.invokespecial(ClassDesc.of(PyStr.class.getCanonicalName()), "<init>",
                MethodTypeDesc.of(CDisCompiler.VOID_CD, ClassDesc.of(String.class.getCanonicalName())));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyStr_" + CDisCompiler.arbitraryTextToJavaIdentifierName(value);
    }

    @Override
    public PyAttributes attributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyType type() {
        throw new UnsupportedOperationException();
    }
}
