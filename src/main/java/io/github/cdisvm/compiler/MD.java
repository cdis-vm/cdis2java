package io.github.cdisvm.compiler;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

public final class MD {
    private MD() {}

    public static MethodTypeDesc of(Class<?> returnType, Class<?>... paramTypes) {
        var paramClassDesc = new ClassDesc[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramClassDesc[i] = CD.of(paramTypes[i]);
        }
        return MethodTypeDesc.of(CD.of(returnType), paramClassDesc);
    }
}
