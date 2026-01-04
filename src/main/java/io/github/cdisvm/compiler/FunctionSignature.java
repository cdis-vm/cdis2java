package io.github.cdisvm.compiler;

import java.util.List;

import io.github.cdisvm.runtime.PyType;

public record FunctionSignature(List<FunctionParameter> parameters, PyType returnType) {
    public static final String SIGNATURE_PACKAGE = "io.github.cdisvm.codegen.signature.";
    public static final String CALL_BUILDER_PACKAGE = "io.github.cdisvm.codegen.callbuilder.";
    public static final String CALLABLE_PACKAGE = "io.github.cdisvm.codegen.callable.";
    public static String getPositionalArgumentInterfaceName(int argumentIndex) {
        return SIGNATURE_PACKAGE + "$" + argumentIndex;
    }

    public static String getKeywordArgumentInterfaceName(String argumentName) {
        return SIGNATURE_PACKAGE + argumentName;
    }
}
