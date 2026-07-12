package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.constant.ClassDesc;

import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.annotation.PyBuiltin;

public record PyNone() implements PyConstant {
    @PyBuiltin("None")
    public static final PyNone INSTANCE = new PyNone();

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.fieldAccess(Opcode.GETSTATIC, ClassDesc.of(PyNone.class.getCanonicalName()),
                "INSTANCE", ClassDesc.of(PyNone.class.getCanonicalName()));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyNone_INSTANT";
    }

    @Override
    public String toString() {
        return "None";
    }
}
