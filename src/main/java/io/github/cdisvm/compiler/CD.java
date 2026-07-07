package io.github.cdisvm.compiler;

import java.lang.constant.ClassDesc;
import java.util.List;

import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyList;
import io.github.cdisvm.runtime.builtin.PyTuple;

public final class CD {
    public static final ClassDesc VOID = ClassDesc.ofDescriptor("V");
    public static final ClassDesc INT = ClassDesc.ofDescriptor("I");
    public static final ClassDesc BOOLEAN = ClassDesc.ofDescriptor("Z");
    public static final ClassDesc OBJECT = CD.of(Object.class);
    public static final ClassDesc LIST = CD.of(List.class);
    public static final ClassDesc PY_OBJECT = CD.of(PyObject.class);
    public static final ClassDesc PY_CALLABLE = CD.of(PyCallable.class);
    public static final ClassDesc PY_CALL_BUILDER = CD.of(PyCallBuilder.class);
    public static final ClassDesc PY_TUPLE = CD.of(PyTuple.class);
    public static final ClassDesc PY_LIST = CD.of(PyList.class);

    private CD() {}

    public static ClassDesc of(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (void.class.equals(clazz)) {
                return VOID;
            } else if (boolean.class.equals(clazz)) {
                return BOOLEAN;
            } else if (int.class.equals(clazz)) {
                return INT;
            }
        }
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
