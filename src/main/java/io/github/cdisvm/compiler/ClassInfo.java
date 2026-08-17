package io.github.cdisvm.compiler;

import java.util.Map;

import io.github.cdisvm.runtime.PyType;

public record ClassInfo(String simpleName,
                        String qualifiedName,
                        Map<String, PyType> classAttributeToType,
                        Map<String, PyType> instanceAttributeToType,
                        Map<String, Object> classAttributeToDefaultValue) {
    public ClassInfo {
        simpleName = CDisCompiler.arbitraryTextToJavaIdentifierName(simpleName);
        qualifiedName = CDisCompiler.arbitraryTextToJavaIdentifierName(qualifiedName);
    }
}
