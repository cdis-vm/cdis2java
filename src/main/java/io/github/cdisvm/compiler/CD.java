package io.github.cdisvm.compiler;

import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.util.List;

import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyGlobal;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyIterator;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyDict;
import io.github.cdisvm.runtime.builtin.PyList;
import io.github.cdisvm.runtime.builtin.PySet;
import io.github.cdisvm.runtime.builtin.PyTuple;

public final class CD {
    public static final ClassDesc VOID = ClassDesc.ofDescriptor("V");
    public static final ClassDesc INT = ClassDesc.ofDescriptor("I");
    public static final ClassDesc LONG = ClassDesc.ofDescriptor("J");
    public static final ClassDesc BOOLEAN = ClassDesc.ofDescriptor("Z");
    public static final ClassDesc DOUBLE = ClassDesc.ofDescriptor("D");
    public static final ClassDesc OBJECT = CD.of(Object.class);
    public static final ClassDesc LIST = CD.of(List.class);
    public static final ClassDesc PY_OBJECT = CD.of(PyObject.class);
    public static final ClassDesc PY_CALLABLE = CD.of(PyCallable.class);
    public static final ClassDesc PY_CALL_BUILDER = CD.of(PyCallBuilder.class);
    public static final ClassDesc PY_TUPLE = CD.of(PyTuple.class);
    public static final ClassDesc PY_LIST = CD.of(PyList.class);
    public static final ClassDesc PY_DICT= CD.of(PyDict.class);
    public static final ClassDesc PY_SET = CD.of(PySet.class);
    public static final ClassDesc PY_CELL = CD.of(PyCell.class);
    public static final ClassDesc PY_GLOBAL = CD.of(PyGlobal.class);
    public static final ClassDesc PY_BOOL = CD.of(PyBool.class);
    public static final ClassDesc PY_ITERABLE = CD.of(PyIterable.class);
    public static final ClassDesc PY_ITERATOR = CD.of(PyIterator.class);
    // class is generated at runtime
    public static final String PY_BUILTINS_NAME = "io.github.cdisvm.codegen.builtins.PyBuiltins";
    public static final ClassDesc PY_BUILTINS = ClassDesc.of(PY_BUILTINS_NAME);

    private CD() {}

    public static ClassDesc of(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (void.class.equals(clazz)) {
                return VOID;
            } else if (boolean.class.equals(clazz)) {
                return BOOLEAN;
            } else if (int.class.equals(clazz)) {
                return INT;
            } else if (long.class.equals(clazz)) {
                return LONG;
            } else if (double.class.equals(clazz)) {
                return DOUBLE;
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
