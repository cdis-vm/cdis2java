package io.github.cdisvm.compiler;

import java.lang.constant.ClassDesc;

import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyObject;

public final class CD {
    public static final ClassDesc VOID = ClassDesc.ofDescriptor("V");
    public static final ClassDesc INT = ClassDesc.ofDescriptor("I");
    public static final ClassDesc BOOLEAN = ClassDesc.ofDescriptor("Z");
    public static final ClassDesc OBJECT = CD.of(Object.class);
    public static final ClassDesc PY_OBJECT = CD.of(PyObject.class);
    public static final ClassDesc PY_CALLABLE = CD.of(PyCallable.class);
    public static final ClassDesc PY_CALL_BUILDER = CD.of(PyCallBuilder.class);

    private CD() {}

    public static ClassDesc of(Class<?> clazz) {
        return ClassDesc.of(clazz.getCanonicalName());
    }

    public static ClassDesc forFunctionParameterName(String parameterName) {
        return ClassDesc.of(
                FunctionSignature.getKeywordArgumentInterfaceName(parameterName));
    }

    public static ClassDesc forFunctionParameterIndex(int index) {
        return ClassDesc.of(
                FunctionSignature.getPositionalArgumentInterfaceName(index));
    }
}
