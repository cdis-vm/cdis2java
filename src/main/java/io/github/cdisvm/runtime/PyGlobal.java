package io.github.cdisvm.runtime;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.util.Objects;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.MD;

public final class PyGlobal {
    public static final String GLOBAL_PACKAGE = "io.github.cdisvm.codegen.globals";
    public static final String GLOBAL_FIELD_NAME = "value";

    private final String globalName;
    private final long globalsDictId;
    private PyObject value;

    public PyGlobal(String globalName, long globalsDictId) {
        this.globalName = globalName;
        this.globalsDictId = globalsDictId;
        this.value = null;
    }

    public PyGlobal(String globalName, long globalsDictId, PyObject value) {
        this.globalName = globalName;
        this.globalsDictId = globalsDictId;
        this.value = value;
    }

    public long getGlobalsDictId() {
        return globalsDictId;
    }

    public String getClassName() {
        return getClassName(globalsDictId, globalName);
    }

    public static String getClassName(long globalsDictId, String globalName) {
        return "%s.global%d.%s".formatted(GLOBAL_PACKAGE, globalsDictId, globalName);
    }

    public void read(CodeBuilder codeBuilder) {
        var globalClass = ClassDesc.of(getClassName());
        codeBuilder.getstatic(globalClass, GLOBAL_FIELD_NAME, CD.PY_GLOBAL);
        codeBuilder.invokevirtual(CD.PY_GLOBAL, "getValue", MD.of(PyObject.class));
    }

    public void write(CodeBuilder codeBuilder) {
        var globalClass = ClassDesc.of(getClassName());
        codeBuilder.getstatic(globalClass, GLOBAL_FIELD_NAME, CD.PY_GLOBAL);
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.PY_GLOBAL, "setValue", MD.of(void.class, PyObject.class));
    }

    public void delete(CodeBuilder codeBuilder) {
        var globalClass = ClassDesc.of(getClassName());
        codeBuilder.getstatic(globalClass, GLOBAL_FIELD_NAME, CD.PY_GLOBAL);
        codeBuilder.aconst_null();
        codeBuilder.invokevirtual(CD.PY_GLOBAL, "setValue", MD.of(void.class, PyObject.class));
    }

    public PyObject getValue() {
        return value;
    }

    public void setValue(PyObject value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PyGlobal pyGlobal))
            return false;
        return globalsDictId == pyGlobal.globalsDictId &&
                pyGlobal.globalName.equals(globalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalsDictId, globalName);
    }
}
