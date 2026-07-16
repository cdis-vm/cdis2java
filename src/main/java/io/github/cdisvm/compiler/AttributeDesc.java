package io.github.cdisvm.compiler;

import java.lang.constant.ClassDesc;

public record AttributeDesc(ClassDesc interfaceDesc,
                            String attributeName) {
    public String getter() {
        return "get$%s".formatted(attributeName);
    }

    public String setter() {
        return "set$%s".formatted(attributeName);
    }

    public String delete() {
        return "delete$%s".formatted(attributeName);
    }
}
