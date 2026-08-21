package io.github.cdisvm.runtime;

import java.lang.classfile.CodeBuilder;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CDisCompiler;

@NullMarked
public interface PyType extends PyObject, PyCallable, PyConstant {
    static PyType of(Class<? extends PyObject> clazz) {
        return null; // TODO: Implement
    }

    List<PyType> mro();

    boolean instanceCheck(PyObject instance);
    boolean subclassCheck(PyType clazz);
    PyObject newInstance();

    default void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.getstatic(CD.of(getClass()), "INSTANCE", CD.of(getClass()));
    }

    default String getJavaIdentifierName() {
        return "$Type$%s".formatted(
                CDisCompiler.arbitraryTextToJavaIdentifierName(Objects.toIdentityString(this)));
    }
}
