package io.github.cdisvm.runtime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyEmptyAttributes;
import io.github.cdisvm.runtime.builtin.PyInt;
import io.github.cdisvm.runtime.builtin.PyNone;
import io.github.cdisvm.runtime.builtin.PyObjectType;
import io.github.cdisvm.runtime.builtin.PyStr;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PyDefault {
    enum Type {
        NULL, INT, STRING, BOOL, NONE;

        public PyObject getValue(String value) {
            return switch (this) {
                case INT -> PyInt.of(Long.parseLong(value));
                case STRING -> new PyStr(value);
                case BOOL -> PyBool.of(Boolean.parseBoolean(value));
                case NONE -> PyNone.INSTANCE;
                case NULL -> NullConstant.INSTANCE;
            };
        }
    }
    class NullConstant implements PyConstant {
        private static final NullConstant INSTANCE = new NullConstant();
        private NullConstant() {}

        @Override
        public void loadValueOntoStack(CodeBuilder codeBuilder) {
            codeBuilder.aconst_null();
        }

        @Override
        public String getJavaIdentifierName() {
            return "NullValue";
        }

        @Override
        public PyAttributes pyAttributes() {
            return PyEmptyAttributes.INSTANCE;
        }

        @Override
        public PyType pyType() {
            return PyObjectType.INSTANCE;
        }

        @Override
        public PyBool pyTruth() {
            return PyBool.FALSE;
        }
    }
    Type type();
    String value();
}
