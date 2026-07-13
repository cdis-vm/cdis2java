package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.constant.ClassDesc;

import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;

public record PyNotImplemented() implements PyConstant {
    public static PyType type;
    @PyBuiltin("NotImplemented")
    public static final PyNotImplemented INSTANCE = new PyNotImplemented();

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.fieldAccess(Opcode.GETSTATIC, ClassDesc.of(PyNotImplemented.class.getCanonicalName()),
                "INSTANCE", ClassDesc.of(PyNotImplemented.class.getCanonicalName()));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyNotImplemented_INSTANT";
    }
}
