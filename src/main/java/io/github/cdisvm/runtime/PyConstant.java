package io.github.cdisvm.runtime;

import java.lang.classfile.CodeBuilder;

public interface PyConstant extends PyObject {
    void loadValueOntoStack(CodeBuilder codeBuilder);
    String getJavaIdentifierName();
}
