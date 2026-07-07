package io.github.cdisvm.compiler;

import java.util.ArrayList;
import java.util.List;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

public record FunctionSignature(List<FunctionParameter> parameters, PyType returnType) {
    public static final String SIGNATURE_PACKAGE = "io.github.cdisvm.codegen.signature.";
    public static final String CALL_BUILDER_PACKAGE = "io.github.cdisvm.codegen.callbuilder.";
    public static final String CALLABLE_PACKAGE = "io.github.cdisvm.codegen.callable.";

    public static Builder builder() {
        return new Builder();
    }

    public static String getPositionalArgumentInterfaceName(int argumentIndex) {
        return SIGNATURE_PACKAGE + "$" + argumentIndex;
    }

    public static String getKeywordArgumentInterfaceName(String argumentName) {
        return SIGNATURE_PACKAGE + argumentName;
    }

    public static class Builder {
        private List<FunctionParameter> parameters = new ArrayList<>();
        private PyType returnType;

        public Builder param(FunctionParameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public Builder param(String name, Class<? extends PyObject> type) {
            parameters.add(new FunctionParameter(
                    parameters.size(),
                    name,
                    ParameterKind.POSITIONAL_OR_KEYWORD,
                    PyType.of(type),
                    null
            ));
            return this;
        }

        public Builder param(String name, Class<? extends PyObject> type, PyObject defaultValue) {
            parameters.add(new FunctionParameter(
                    parameters.size(),
                    name,
                    ParameterKind.POSITIONAL_OR_KEYWORD,
                    PyType.of(type),
                    defaultValue
            ));
            return this;
        }

        public Builder returningType(PyType returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder returning(Class<? extends PyObject> returnType) {
            this.returnType = PyType.of(returnType);
            return this;
        }

        public FunctionSignature build() {
            return new FunctionSignature(parameters, returnType);
        }
    }
}
